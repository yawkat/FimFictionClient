package at.yawk.fimfiction.examples.cmdsearch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;

import at.yawk.fimfiction.EnumCategory;
import at.yawk.fimfiction.EnumCharacter;
import at.yawk.fimfiction.EnumDownloadType;
import at.yawk.fimfiction.EnumSearchOrder;
import at.yawk.fimfiction.EnumStoryContentRating;
import at.yawk.fimfiction.EnumStoryMatureCategories;
import at.yawk.fimfiction.FimFictionConnectionStandard;
import at.yawk.fimfiction.IFimFictionConnection;
import at.yawk.fimfiction.SearchRequestBuilder;
import at.yawk.fimfiction.Searches;
import at.yawk.fimfiction.Stories;
import at.yawk.fimfiction.Story;
import at.yawk.fimfiction.Util;

public class CMDSearch implements Runnable {
	
	private final File					configFile;
	private final Map<String, String>	config	= new HashMap<String, String>();
	
	private CMDSearch(final File configFile) {
		this.configFile = configFile;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length == 1) {
			final File configFile = new File(args[0]);
			if(configFile.exists()) {
				new CMDSearch(configFile).run();
			} else {
				System.err.println("Config file not found");
			}
		} else {
			System.err.println("Invalid arguments. Syntax: <java> <config file name>");
		}
	}
	
	@Override
	public void run() {
		try {
			loadConfig();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		final SearchRequestBuilder request = new SearchRequestBuilder();
		final IFimFictionConnection connection = new FimFictionConnectionStandard();
		File outputDir = new File("downloads");
		EnumDownloadType type = EnumDownloadType.EPUB;
		
		for(final String key : config.keySet()) {
			final String value = config.get(key);
			// I am using if here for JRE 6 compatibility.
			if(key.equals("search")) {
				request.setSearchTerm(value);
			} else if(key.equals("mature_category")) {
				request.setMatureCategories(EnumStoryMatureCategories.parse(value.toLowerCase()));
			} else if(key.equals("order")) {
				request.setSearchOrder(EnumSearchOrder.parse(value));
			} else if(key.equals("content_rating")) {
				request.setContentRating(EnumStoryContentRating.parse(value));
			} else if(key.equals("completed")) {
				request.setMustBeCompleted(Boolean.parseBoolean(value));
			} else if(key.equals("minimum_words")) {
				request.setMinimumWords(Integer.parseInt(value));
			} else if(key.equals("maximum_words")) {
				request.setMaximumWords(Integer.parseInt(value));
			} else if(key.startsWith("category_")) {
				final String cname = key.substring(9);
				request.getCategories().put(EnumCategory.parse(cname), Boolean.parseBoolean(value));
			} else if(key.startsWith("character_")) {
				final String cname = key.substring(10);
				request.getCharacters().put(EnumCharacter.parse(cname), Boolean.parseBoolean(value));
			} else if(key.equals("output")) {
				outputDir = new File(value);
			} else if(key.equals("format")) {
				type = EnumDownloadType.parse(value);
			}
		}
		
		outputDir.mkdirs();
		final File odir = outputDir;
		final EnumDownloadType type0 = type;
		
		try {
			final PrintWriter pw = new PrintWriter(new File(outputDir, "0000 - stories.txt"));
			try {
				final Iterator<Story> i = Searches.parseFullSearchPartially(Util.FIMFICTION + "index.php?" + request.getRequest(), connection, 0);
				final BlockingQueue<Story> download = new LinkedBlockingQueue<Story>();
				new Thread(new Runnable() {
					@Override
					public void run() {
						final Executor ex = Executors.newFixedThreadPool(20);
						int c = 0;
						while(true) {
							final Story s = download.poll();
							if(s == null)
								try {
									TimeUnit.MILLISECONDS.sleep(200L);
								} catch(InterruptedException e1) {
									e1.printStackTrace();
								}
							else
								try {
									System.out.println("Downloading #" + ++c + " (" + s.getId() + ")");
									Stories.updateStory(s, connection);
									String index = Integer.toString(c);
									while(index.length() < 4)
										index = '0' + index;
									final File outputFile = new File(odir, index + " - " + s.getTitle().replaceAll("[^\\w ]", "") + "." + type0.getFileType());
									ex.execute(new Runnable() {
										@Override
										public void run() {
											try {
												Stories.downloadStory(s, new FileOutputStream(outputFile), type0, connection);
											} catch(FileNotFoundException e) {
												e.printStackTrace();
											} catch(IOException e) {
												e.printStackTrace();
											}
										}
									});
									{
										pw.println(c);
										pw.println(s.getTitle().replace("\n", "").replace("\r", ""));
										pw.println(s.getDescription().replace("\n", "").replace("\r", ""));
										pw.println();
										pw.flush();
									}
								} catch(IOException e) {
									e.printStackTrace();
								} catch(JSONException e) {
									e.printStackTrace();
								}
						}
					}
				}).start();
				while(i.hasNext()) {
					final Story s = i.next();
					download.add(s);
					while(download.size() > 20)
						TimeUnit.MILLISECONDS.sleep(200L);
				}
			} catch(InterruptedException e1) {
				e1.printStackTrace();
			} finally {
				pw.close();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadConfig() throws FileNotFoundException {
		final Scanner s = new Scanner(configFile);
		try {
			while(s.hasNext()) {
				String pair = s.nextLine();
				{
					pair += "T";
					pair = pair.trim();
					pair = pair.substring(0, pair.length() - 1);
				}
				if(!pair.startsWith("#") && pair.contains("=")) {
					final String key = pair.substring(0, pair.indexOf('=')).toLowerCase();
					final String value = pair.substring(pair.indexOf('=') + 1);
					if(value.length() > 0)
						config.put(key, value);
				}
			}
		} finally {
			s.close();
		}
	}
}
