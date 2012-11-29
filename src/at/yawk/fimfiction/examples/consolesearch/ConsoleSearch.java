package at.yawk.fimfiction.examples.consolesearch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONException;

import at.yawk.fimfiction.EnumCategory;
import at.yawk.fimfiction.EnumCharacter;
import at.yawk.fimfiction.EnumDownloadType;
import at.yawk.fimfiction.EnumSearchOrder;
import at.yawk.fimfiction.EnumStoryContentRating;
import at.yawk.fimfiction.EnumStoryMatureCategories;
import at.yawk.fimfiction.FimFictionConnectionStandard;
import at.yawk.fimfiction.SearchRequestBuilder;
import at.yawk.fimfiction.Searches;
import at.yawk.fimfiction.Stories;
import at.yawk.fimfiction.Story;
import at.yawk.fimfiction.Util;

public class ConsoleSearch {
	public static void main(String[] args) {
		final Object[] enums;
		{
			final Collection<Object> c = new ArrayList<Object>();
			c.addAll(Arrays.asList(EnumDownloadType.values()));
			c.addAll(Arrays.asList(EnumCharacter.values()));
			c.addAll(Arrays.asList(EnumSearchOrder.values()));
			c.addAll(Arrays.asList(EnumCategory.values()));
			c.addAll(Arrays.asList(EnumStoryContentRating.values()));
			c.addAll(Arrays.asList(EnumStoryMatureCategories.values()));
			enums = c.toArray();
		}
		final SearchRequestBuilder builder = new SearchRequestBuilder();
		String outputdir = "output";
		EnumDownloadType download = EnumDownloadType.EPUB;
		for(String s : args) {
			for(Object o : enums) {
				final String[] as = s.split(":");
				if(as[0].equalsIgnoreCase("search")) {
					builder.setSearchTerm(as[1]);
					System.out.println("Set search to " + as[1]);
					break;
				} else if(as[0].equalsIgnoreCase("output")) {
					outputdir = as[1];
					System.out.println("Set output dir to " + as[1]);
					break;
				} else if(o.toString().toLowerCase().contains(as[0])) {
					if(o instanceof EnumSearchOrder) {
						builder.setSearchOrder((EnumSearchOrder)o);
						System.out.println("Set search order to " + o);
					} else if(o instanceof EnumCharacter) {
						final boolean b = as.length == 1 ? true : as[1].equals("+");
						builder.getCharacters().put((EnumCharacter)o, b);
						System.out.println("Set character " + o + " to " + b);
					} else if(o instanceof EnumCategory) {
						final boolean b = as.length == 1 ? true : as[1].equals("+");
						builder.getCategories().put((EnumCategory)o, b);
						System.out.println("Set category " + o + " to " + b);
					} else if(o instanceof EnumStoryContentRating) {
						builder.setContentRating((EnumStoryContentRating)o);
						System.out.println("Set content rating to " + o);
					} else if(o instanceof EnumStoryMatureCategories) {
						builder.setMatureCategories((EnumStoryMatureCategories)o);
						System.out.println("Set mature category to " + o);
					} else if(o instanceof EnumDownloadType) {
						download = (EnumDownloadType)o;
						System.out.println("Set output format to " + o);
					}
					break;
				}
			}
		}
		
		{
			final File outdir = new File(outputdir);
			outdir.mkdirs();
			final EnumDownloadType dtype = download;
			final BlockingQueue<Entry> toDownload = new LinkedBlockingQueue<Entry>();
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						final Iterator<Story> i = Searches.parseFullSearchPartially(Util.FIMFICTION + "index.php?" + builder.getRequest(), new FimFictionConnectionStandard(), 0);
						int id = 0;
						while(i.hasNext()) {
							toDownload.add(new Entry(i.next(), id++));
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
			try {
				final PrintWriter pw = new PrintWriter(new File(outdir, "0000 - stories.txt"));
				final AtomicInteger nextEntry = new AtomicInteger();
				for(int i = 0; i < 8; i++)
					new Thread(new Runnable() {
						@Override
						public void run() {
							while(true) {
								try {
									final Entry s = toDownload.take();
									Stories.updateStory(s.s, new FimFictionConnectionStandard());
									System.out.println("Downloading " + (s.id + 1) + " (" + s.s.getTitle() + ")");
									String is = Integer.toString(s.id + 1);
									while(is.length() < 4)
										is = "0" + is;
									Stories.downloadStory(s.s, new FileOutputStream(new File(outdir, is + " - " + s.s.getTitle().replaceAll("[^\\w ]", "") + "." + dtype.getFileType())), dtype, new FimFictionConnectionStandard());
									while(nextEntry.get() < s.id)
										Thread.sleep(200L);
									pw.println(is);
									pw.println("Title: " + s.s.getTitle());
									pw.println("SDescription: " + s.s.getShortDescription().replace("\n", "").replace("\r", ""));
									pw.flush();
									pw.println();
									nextEntry.incrementAndGet();
								} catch(InterruptedException e) {
									e.printStackTrace();
								} catch(IOException e) {
									e.printStackTrace();
								} catch(JSONException e) {
									e.printStackTrace();
								}
							}
						}
					}).start();
			} catch(FileNotFoundException f) {
				f.printStackTrace();
			}
		}
	}
	
	private static class Entry {
		final Story	s;
		final int	id;
		
		private Entry(Story s, int id) {
			this.s = s;
			this.id = id;
		}
	}
}
