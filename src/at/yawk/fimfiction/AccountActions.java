package at.yawk.fimfiction;

import java.io.IOException;
import java.net.URL;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class AccountActions {
	public static void markReadLater(final IFimFictionConnection ffc, final boolean readLater, final Story s) throws IOException {
		final IURLConnection urlc = ffc.getConnection(new URL(Util.FIMFICTION + "ajax/add_read_it_later.php"));
		urlc.connect();
		urlc.getOutputStream().write(("story=" + s.getId() + "&selected=" + (readLater ? 1 : 0)).getBytes());
		Util.clearStream(urlc.getInputStream());
	}
	
	public static void markFavorite(final IFimFictionConnection ffc, final boolean favorite, final boolean email, final Story s) throws IOException {
		final IURLConnection urlc = ffc.getConnection(new URL(Util.FIMFICTION + "ajax/add_favourite.php"));
		urlc.connect();
		urlc.getOutputStream().write(("story=" + s.getId() + "&selected=" + (favorite ? 1 : 0) + "&email=" + (email ? 1 : 0)).getBytes());
		Util.clearStream(urlc.getInputStream());
	}
	
	public static boolean toggleRead(final IFimFictionConnection ffc, final Chapter c) throws IOException {
		final IURLConnection urlc = ffc.getConnection(new URL(Util.FIMFICTION + "ajax/toggle_read.php"));
		urlc.connect();
		urlc.getOutputStream().write(("chapter=" + c.getId()).getBytes());
		return Util.readFully(urlc.getInputStream()).trim().endsWith("tick.png");
	}
	
	public static void setLike(final IFimFictionConnection ffc, final boolean isLike, final String token, final Story s) throws IOException {
		final IURLConnection urlc = ffc.getConnection(new URL(Util.FIMFICTION + "rate.php"));
		urlc.connect();
		urlc.getOutputStream().write(("story=" + s.getId() + "&rating=" + (isLike ? 100 : 0) + "&ip=" + token).getBytes());
		Util.clearStream(urlc.getInputStream());
	}
	
	public static String getLikeToken(final IFimFictionConnection ffc, final Story s) throws IOException {
		final Document d = Util.getHTML(ffc.getConnection(new URL(Util.FIMFICTION + "story/" + s.getId())));
		final Elements es = d.getElementsByClass("like_button");
		if(es.size() > 0) {
			final String oc = es.get(0).attr("onclick");
			return oc.substring(oc.indexOf('\'') + 1, oc.lastIndexOf('\''));
		} else {
			return null;
		}
	}
	
	public static void setLike(final IFimFictionConnection ffc, final boolean isLike, final Story s) throws IOException {
		setLike(ffc, isLike, getLikeToken(ffc, s), s);
	}
	
	public static boolean[] getHasRead(final IFimFictionConnection ffc, final Story s) throws IOException {
		final Document d = Util.getHTML(ffc.getConnection(new URL(Util.FIMFICTION + "story/" + s.getId())));
		final Elements chapters = d.getElementsByClass("chapters").get(0).getElementsByClass("chapter_container");
		final boolean[] ab = new boolean[chapters.size()];
		for(int i = 0; i < ab.length; i++) {
			ab[i] = chapters.get(i).getElementsByTag("img").get(3).attr("src").endsWith("tick.png");
		}
		return ab;
	}
}
