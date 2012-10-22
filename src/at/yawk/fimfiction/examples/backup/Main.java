package at.yawk.fimfiction.examples.backup;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.json.JSONException;

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

public class Main implements Runnable {
	private final IStoryDateAccess		dateAccess		= new RandomFileStoryDateAccess();
	private final IFimFictionConnection	ffc				= new FimFictionConnectionStandard();
	final static File					rootDir			= new File(".");
	private final XStream				xs				= XMLHelper.getXStreamInstance();
	private final Logger				logger			= Logger.getAnonymousLogger();
	private final static String			LINE_SEPARATOR	= System.getProperty("line.separator");
	
	private Main() {
		rootDir.mkdirs();
		xs.alias("storycache", StandardStoryDateAccess.dataFileEntryClass());
		try {
			final Handler fh = new StreamHandler(new FileOutputStream(new File(rootDir, "all.log")), new Formatter() {
				@Override
				public String format(LogRecord record) {
					final StringBuilder sb = new StringBuilder("[");
					sb.append(new SimpleDateFormat("yyyy-MM-dd kk:mm:s" + "s").format(new Date(record.getMillis())));
					sb.append("] ");
					sb.append(record.getMessage());
					sb.append(LINE_SEPARATOR);
					return sb.toString();
				}
			});
			fh.setLevel(Level.ALL);
			logger.addHandler(fh);
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		final Handler fh = new ConsoleHandler();
		fh.setFormatter(new Formatter() {
			@Override
			public String format(LogRecord record) {
				return record.getMessage() + LINE_SEPARATOR;
			}
		});
		fh.setLevel(Level.ALL);
		logger.addHandler(fh);
		logger.setUseParentHandlers(false);
	}
	
	private Story updateStory(final Story s) throws MalformedURLException, IOException, JSONException {
		final Story n = new Story(s.getId());
		Stories.updateStory(n, ffc);
		if(!n.getModifyTime().equals(s.getModifyTime())) {
			logger.log(Level.INFO, "Updating story " + n.getId() + " (" + n.getTitle() + ")");
			final File storyFolder = new File(rootDir, "stories/" + Integer.toString(n.getId()) + "/" + Long.toString(n.getModifyTime().getTime() / 1000L));
			storyFolder.mkdirs();
			final OutputStream fos = new FileOutputStream(new File(storyFolder, "story.xml"));
			fos.write(xs.toXML(n).getBytes());
			fos.flush();
			fos.close();
			if(s.getChapters() == null || !Arrays.equals(s.getChapters(), n.getChapters())) {
				final OutputStream os = new FileOutputStream(new File(storyFolder, "story.epub"));
				Stories.downloadStory(n, os, EnumDownloadType.EPUB, ffc);
				os.flush();
				os.close();
			}
			clearStoryMemory(n);
		}
		return n;
	}
	
	private void loadData() {
		logger.log(Level.INFO, "Loading...");
		try {
			dateAccess.load(rootDir, xs);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private void saveData() throws IOException {
		logger.log(Level.INFO, "Saving...");
		dateAccess.save(rootDir, xs);
	}
	
	public static void main(final String[] args) {
		new Main().run();
	}
	
	private void startUpdateThread() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final String search = Util.FIMFICTION + "index.php?" + new SearchRequestBuilder().setSearchOrder(EnumSearchOrder.RATING).getRequest();
				while(true) {
					try {
						final Iterator<Story> i = Searches.parseFullSearchPartially(search, ffc, 0);
						while(i.hasNext()) {
							final Story s = i.next();
							if(dateAccess.hasStory(s.getId())) {
								final Story t = dateAccess.getStoryForIndex(s.getId());
								try {
									final Story u;
									dateAccess.setStoryForIndex(s.getId(), u = updateStory(t));
									if(t.getModifyTime() != null && u.getModifyTime().getTime() == t.getModifyTime().getTime())
										break;
								} catch(IOException | JSONException e) {
									logException(Level.WARNING, e);
								}
							}
						}
						logger.log(Level.INFO, "Done updating, sleeping one minute.");
						TimeUnit.MINUTES.sleep(1L);
					} catch(Exception e) {
						logException(Level.WARNING, e);
					}
				}
			}
		}).start();
		new Thread(new Runnable() {
			@Override
			public void run() {
				final AtomicInteger runningDownloaders = new AtomicInteger(0);
				final Executor e = Executors.newFixedThreadPool(10);
				while(true) {
					try {
						final Collection<Story> c = new ArrayList<>(dateAccess.getAllStoriesInMemory());
						for(final Story s : c) {
							if(s.getModifyTime() == null || s.getModifyTime().getTime() == 0)
								e.execute(new Runnable() {
									@Override
									public void run() {
										runningDownloaders.incrementAndGet();
										try {
											dateAccess.setStoryForIndex(s.getId(), updateStory(s));
										} catch(IOException | JSONException e) {
											e.printStackTrace();
										} finally {
											runningDownloaders.decrementAndGet();
										}
									}
								});
						}
						while(runningDownloaders.get() > 0)
							try {
								TimeUnit.SECONDS.sleep(2L);
							} catch(InterruptedException e1) {
								logException(Level.WARNING, e1);
							}
						logger.log(Level.INFO, "Done downloading new stories, sleeping one minute.");
						TimeUnit.MINUTES.sleep(1L);
					} catch(Exception e1) {
						logException(Level.WARNING, e1);
					}
				}
			}
		}).start();
	}
	
	private void startAddingThread() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean isFirstRun = true;
				final String search = Util.FIMFICTION + "index.php?" + new SearchRequestBuilder().setSearchOrder(EnumSearchOrder.FIRST_POSTED_DATE).getRequest();
				while(true) {
					try {
						final Iterator<Story> i = Searches.parseFullSearchPartially(search, ffc, isFirstRun ? Math.max(0, dateAccess.getTotalStoryCount() - 10) : 0);
						while(i.hasNext()) {
							final Story s = i.next();
							if(!dateAccess.hasStory(s.getId())) {
								logger.log(Level.INFO, "Adding story " + s.getId());
								dateAccess.setStoryForIndex(s.getId(), s);
							} else if(!isFirstRun) {
								break;
							}
						}
						logger.log(Level.INFO, "Done adding new stories, sleeping one minute.");
						TimeUnit.MINUTES.sleep(1L);
						isFirstRun = false;
					} catch(Exception e) {
						logException(Level.WARNING, e);
					}
				}
			}
		}).start();
	}
	
	@SuppressWarnings("serial")
	@Override
	public void run() {
		loadData();
		startSaving();
		startAddingThread();
		startUpdateThread();
		if(!dateAccess.isSlowAccess()) {
			final JFrame jf = new JFrame("download");
			jf.add(new JComponent() {
				{
					setPreferredSize(new Dimension(250 * 2, 250 * 2));
					setBackground(Color.WHITE);
				}
				
				@Override
				public void paintComponent(Graphics g) {
					final int width = getWidth() / 2;
					final int height = getHeight() / 2;
					int i = 0;
					for(int x = 0; x < width; x++) {
						for(int y = 0; y < height; y++) {
							final int id = x + y * width;
							final Story s = dateAccess.getStoryForIndex(id);
							if(s != null) {
								final boolean b = s.getModifyTime() == null || s.getModifyTime().getTime() == 0;
								g.setColor(b ? Color.RED : Color.BLUE);
								g.fillRect(x * 2, y * 2, 2, 2);
								if(b)
									i++;
							}
						}
					}
					g.setColor(Color.BLACK);
					g.drawString("Stories: " + dateAccess.getTotalStoryCount() + " total, " + i + " not downloaded yet", 2, getHeight() - 12);
				}
			});
			jf.validate();
			jf.pack();
			jf.setResizable(false);
			jf.setLocationRelativeTo(null);
			final AtomicBoolean isEnabled = new AtomicBoolean(true);
			jf.addWindowListener(new WindowAdapter() {
				public void windowClosed(WindowEvent e) {
					isEnabled.set(false);
				}
			});
			jf.setBackground(Color.WHITE);
			jf.setVisible(true);
			while(isEnabled.get()) {
				try {
					TimeUnit.SECONDS.sleep(5L);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
				jf.repaint();
			}
		}
	}
	
	private void startSaving() {
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					saveData();
				} catch(IOException e) {
					logException(Level.SEVERE, e);
				}
			}
		};
		Runtime.getRuntime().addShutdownHook(new Thread(r));
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						TimeUnit.MINUTES.sleep(1L);
					} catch(InterruptedException e) {
						logException(Level.WARNING, e);
					}
					r.run();
				}
			}
		}).start();
	}
	
	private void clearStoryMemory(final Story s) {
		s.setAuthor(null);
		s.setContentRating(null);
		s.setDescription(null);
		s.setFullImageLocation(null);
		s.setImageLocation(null);
		s.setShortDescription(null);
		s.setStatus(null);
		s.setTitle(null);
	}
	
	private void logException(Level l, Throwable t) {
		final CharArrayWriter caw = new CharArrayWriter();
		t.printStackTrace(new PrintWriter(caw));
		logger.log(l, caw.toString());
	}
}
