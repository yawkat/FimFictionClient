package at.yawk.fimfiction.examples.downloadall;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONException;

import at.yawk.fimfiction.EnumDownloadType;
import at.yawk.fimfiction.FimFictionConnectionAccount;
import at.yawk.fimfiction.SearchRequestBuilder;
import at.yawk.fimfiction.Searches;
import at.yawk.fimfiction.Stories;
import at.yawk.fimfiction.Story;
import at.yawk.fimfiction.Util;

public class Main {
	
	public static void main(String[] args) throws MalformedURLException, IOException, JSONException, InterruptedException {
		if(args.length == 2) {
			final FimFictionConnectionAccount account = new FimFictionConnectionAccount();
			if(account.login(args[0], args[1])) {
				final Collection<Story> unread = new ArrayList<Story>();
				final Collection<Story> readlater = new ArrayList<Story>();
				final Collection<Story> favorites = new ArrayList<Story>();
				final AtomicInteger running = new AtomicInteger(3);
				final Executor ex = Executors.newFixedThreadPool(10);
				ex.execute(new Runnable() {
					@Override
					public void run() {
						System.out.println("Checking unread favorites...");
						try {
							unread.addAll(Arrays.asList(Searches.parseFullSearch(Util.FIMFICTION + "index.php?" + new SearchRequestBuilder().setMustBeFavorite(true).setMustBeUnread(true).getRequest(), account)));
						} catch(IOException e) {
							e.printStackTrace();
						} finally {
							running.decrementAndGet();
						}
					}
				});
				ex.execute(new Runnable() {
					@Override
					public void run() {
						System.out.println("Checking read later...");
						try {
							readlater.addAll(Arrays.asList(Searches.parseFullSearch(Util.FIMFICTION + "index.php?" + new SearchRequestBuilder().setMustBeReadLater(true).getRequest(), account)));
						} catch(IOException e) {
							e.printStackTrace();
						} finally {
							running.decrementAndGet();
						}
					}
				});
				ex.execute(new Runnable() {
					@Override
					public void run() {
						System.out.println("Checking read favorites...");
						try {
							favorites.addAll(Arrays.asList(Searches.parseFullSearch(Util.FIMFICTION + "index.php?" + new SearchRequestBuilder().setMustBeFavorite(true).getRequest(), account)));
						} catch(IOException e) {
							e.printStackTrace();
						} finally {
							running.decrementAndGet();
						}
					}
				});
				while(running.get() > 0)
					TimeUnit.SECONDS.sleep(1);
				favorites.removeAll(unread);
				final int am = unread.size() + readlater.size() + favorites.size();
				new File("unread").mkdirs();
				new File("readlater").mkdirs();
				new File("favorites").mkdirs();
				final AtomicInteger index = new AtomicInteger();
				for(final Story s : unread) {
					running.incrementAndGet();
					ex.execute(new Runnable() {
						@Override
						public void run() {
							try {
								System.out.println("Downloading (" + index.incrementAndGet() + " of " + am + ")");
								Stories.updateStory(s, account);
								Stories.downloadStory(s, new FileOutputStream("unread/" + s.getTitle().replaceAll("[^\\w ]", "") + ".epub"), EnumDownloadType.EPUB, account);
							} catch(IOException e) {
								e.printStackTrace();
							} catch(JSONException e) {
								e.printStackTrace();
							} finally {
								running.decrementAndGet();
							}
						}
					});
				}
				for(final Story s : readlater) {
					running.incrementAndGet();
					ex.execute(new Runnable() {
						@Override
						public void run() {
							try {
								System.out.println("Downloading (" + index.incrementAndGet() + " of " + am + ")");
								Stories.updateStory(s, account);
								Stories.downloadStory(s, new FileOutputStream("readlater/" + s.getTitle().replaceAll("[^\\w ]", "") + ".epub"), EnumDownloadType.EPUB, account);
							} catch(IOException e) {
								e.printStackTrace();
							} catch(JSONException e) {
								e.printStackTrace();
							} finally {
								running.decrementAndGet();
							}
						}
					});
				}
				for(final Story s : favorites) {
					running.incrementAndGet();
					ex.execute(new Runnable() {
						@Override
						public void run() {
							try {
								System.out.println("Downloading (" + index.incrementAndGet() + " of " + am + ")");
								Stories.updateStory(s, account);
								Stories.downloadStory(s, new FileOutputStream("favorites/" + s.getTitle().replaceAll("[^\\w ]", "") + ".epub"), EnumDownloadType.EPUB, account);
							} catch(IOException e) {
								e.printStackTrace();
							} catch(JSONException e) {
								e.printStackTrace();
							} finally {
								running.decrementAndGet();
							}
						}
					});
				}
				while(running.get() > 0)
					TimeUnit.SECONDS.sleep(1);
				System.exit(0);
			} else {
				System.err.println("Invalid account information.");
			}
		} else {
			System.err.println("Invalid arguments. Usage: <java> <username> <password>");
		}
	}
}
