package at.yawk.fimfiction.examples.backup.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang.IntHashMap;

import com.thoughtworks.xstream.XStream;

import at.yawk.fimfiction.EnumDownloadType;
import at.yawk.fimfiction.EnumSearchOrder;
import at.yawk.fimfiction.FimFictionConnectionStandard;
import at.yawk.fimfiction.IFimFictionConnection;
import at.yawk.fimfiction.SearchRequestBuilder;
import at.yawk.fimfiction.Searches;
import at.yawk.fimfiction.Stories;
import at.yawk.fimfiction.Story;
import at.yawk.fimfiction.Util;
import at.yawk.fimfiction.XMLHelper;

public class Backup implements Runnable {
	private final ReadWriteLock		modifyTimesLock	= new ReentrantReadWriteLock();
	private final IntHashMap<Long>	modifyTimes		= new IntHashMap<>();
	private final AtomicInteger		maximumStoryId	= new AtomicInteger(0);
	
	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		if(args.length == 1) {
			final Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						TimeUnit.SECONDS.sleep(Long.parseLong(args[0]));
						System.exit(0);
					} catch(NumberFormatException e) {
						
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
			t.setDaemon(true);
			t.start();
		}
		new Backup().run();
	}
	
	@Override
	public void run() {
		loadData();
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				saveData();
			}
		}));
		try {
			System.setErr(new PrintStream(new FileOutputStream(new File("system.err.log"), true)));
		} catch(FileNotFoundException e1) {
			e1.printStackTrace();
		}
		System.err.println("");
		System.err.println("---------------------------------------------------");
		System.err.println("");
		System.err.println("Logging:");
		System.err.println("");
		
		final String searchRequestUrl = Util.FIMFICTION + "index.php?" + new SearchRequestBuilder().setSearchOrder(EnumSearchOrder.UPDATE_DATE).getRequest();
		final IFimFictionConnection fimFictionConnection = new FimFictionConnectionStandard();
		fimFictionConnection.setDisplayMature(true);
		final XStream xml = XMLHelper.getXStreamInstance();
		
		final Executor storyDownloader = Executors.newFixedThreadPool(20);
		final AtomicInteger waitingDownloaders = new AtomicInteger(0);
		
		int counter = 0;
		
		while(true) {
			System.out.println("Reading stories (" + ++counter + ")");
			try {
				final Iterator<Story> i = Searches.parseFullSearchPartially(searchRequestUrl, fimFictionConnection, 0);
				while(i.hasNext()) {
					final Story s = i.next();
					
					waitingDownloaders.incrementAndGet();
					storyDownloader.execute(new Runnable() {
						@Override
						public void run() {
							try {
								Stories.updateStory(s, fimFictionConnection);
								
								final boolean b;
								modifyTimesLock.readLock().lock();
								try {
									b = !modifyTimes.containsKey(s.getId()) || modifyTimes.get(s.getId()) != s.getModifyTime().getTime();
								} finally {
									modifyTimesLock.readLock().unlock();
								}
								if(b) {
									System.out.println("Updating story " + s.getId() + " (" + s.getTitle() + ")");
									if(maximumStoryId.get() < s.getId())
										maximumStoryId.set(s.getId());
									modifyTimesLock.writeLock().lock();
									try {
										modifyTimes.put(s.getId(), s.getModifyTime().getTime());
									} finally {
										modifyTimesLock.writeLock().unlock();
									}
									final File dir = getDirectoryForStoryAndDate(s.getId(), s.getModifyTime().getTime());
									dir.mkdirs();
									xml.toXML(s, new FileOutputStream(new File(dir, "story.xml")));
									Stories.downloadStory(s, new FileOutputStream(new File(dir, "story.epub")), EnumDownloadType.EPUB, fimFictionConnection);
								}
							} catch(Exception e) {
								e.printStackTrace();
							} finally {
								waitingDownloaders.decrementAndGet();
							}
						}
					});
				}
				
				System.out.println("Done with search request, waiting for stories to finish downloading.");
				
				while(waitingDownloaders.get() > 0)
					TimeUnit.SECONDS.sleep(5);
			} catch(Exception e) {
				e.printStackTrace();
			}
			saveData();
		}
	}
	
	private File getDirectoryForStoryAndDate(final int storyId, final long modifyTime) {
		return new File("files/stories/" + storyId + "/" + modifyTime);
	}
	
	private final void saveData() {
		try {
			System.out.println("Saving data.");
			final DataOutputStream output = new DataOutputStream(new FileOutputStream(new File("files/stories.dat")));
			try {
				for(int i = 0; i < maximumStoryId.get(); i++) {
					try {
						modifyTimesLock.readLock().lock();
						final Long l;
						try {
							l = modifyTimes.get(i);
						} finally {
							modifyTimesLock.readLock().unlock();
						}
						if(l != null)
							output.writeLong(l);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			} finally {
				output.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.err.println("FAILED TO SAVE DATA. IF THE PROGRAM IS ABORTED WITHOUT PROPER SAVING, ALL STORIES WILL BE REDOWNLOADED ON NEXT STARTUP. That is not good.");
			System.out.println("FAILED TO SAVE DATA. IF THE PROGRAM IS ABORTED WITHOUT PROPER SAVING, ALL STORIES WILL BE REDOWNLOADED ON NEXT STARTUP. That is not good.");
		}
	}
	
	private final void loadData() {
		if(new File("files/stories.dat").exists())
			try {
				System.out.println("Loading data.");
				final DataInputStream input = new DataInputStream(new FileInputStream(new File("files/stories.dat")));
				try {
					int storyId = 0;
					while(input.available() > 0) {
						final long l = input.readLong();
						if(l > 0) {
							modifyTimesLock.writeLock().lock();
							try {
								modifyTimes.put(storyId, l);
							} finally {
								modifyTimesLock.writeLock().unlock();
							}
						}
						storyId++;
					}
					maximumStoryId.set(storyId);
				} finally {
					input.close();
				}
			} catch(Exception e) {
				e.printStackTrace();
				System.err.println("FAILED TO LOAD DATA. IF THE PROGRAM IS RUN WITHOUT PROPER LOADING, ALL STORIES WILL BE REDOWNLOADED. That is not good.");
			}
	}
}
