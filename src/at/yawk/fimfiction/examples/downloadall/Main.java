package at.yawk.fimfiction.examples.downloadall;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.json.JSONException;

import at.yawk.fimfiction.EnumDownloadType;
import at.yawk.fimfiction.FimFictionConnectionAccount;
import at.yawk.fimfiction.SearchRequestBuilder;
import at.yawk.fimfiction.Searches;
import at.yawk.fimfiction.Stories;
import at.yawk.fimfiction.Story;
import at.yawk.fimfiction.Util;

public class Main {
	
	/**
	 * @param args
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws JSONException
	 */
	public static void main(String[] args) throws MalformedURLException, IOException, JSONException {
		if(args.length == 2) {
			final FimFictionConnectionAccount account = new FimFictionConnectionAccount();
			if(account.login(args[0], args[1])) {
				final Collection<Story> c = new ArrayList<>();
				System.out.println("Checking unread favorites...");
				c.addAll(Arrays.asList(Searches.parseFullSearch(Util.FIMFICTION + "index.php?" + new SearchRequestBuilder().setMustBeFavorite(true).setMustBeUnread(true).getRequest(), account)));
				System.out.println("Checking read later...");
				c.addAll(Arrays.asList(Searches.parseFullSearch(Util.FIMFICTION + "index.php?" + new SearchRequestBuilder().setMustBeReadLater(true).getRequest(), account)));
				int index = 0;
				for(final Story s : c) {
					System.out.println("Downloading (" + ++index + " of " + c.size() + ")");
					Stories.updateStory(s, account);
					Stories.downloadStory(s, new FileOutputStream(s.getTitle().replaceAll("\\W", "") + ".epub"), EnumDownloadType.EPUB, account);
				}
			} else {
				System.err.println("Invalid account information.");
			}
		} else {
			System.err.println("Invalid arguments. Usage: <java> <username> <password>");
		}
	}
}
