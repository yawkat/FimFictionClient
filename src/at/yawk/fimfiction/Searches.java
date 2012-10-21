package at.yawk.fimfiction;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Searches {
	public static Story[] parseSearch(final Document d) {
		final Elements es = d.getElementsByClass("content_box");
		final List<Story> l = new ArrayList<Story>(10);
		for(final Element e : es) {
			if(e.hasClass("post_content_box")) {
				final Elements ae = e.getElementsByTag("h2").get(0).getElementsByTag("a");
				final Element a = ae.get(ae.size() - 3);
				final String s = a.attr("href").substring(7);
				l.add(new Story(Integer.parseInt(s.substring(0, s.indexOf('/')))));
			}
		}
		final Story[] as = new Story[l.size()];
		l.toArray(as);
		return as;
	}
	
	public static Story[] parseFullSearch(final String url, final IFimFictionConnection ffc) throws MalformedURLException, IOException {
		final List<Story> l = new ArrayList<Story>();
		final Iterator<Story> i = parseFullSearchPartially(url, ffc, 0);
		while(i.hasNext())
			l.add(i.next());
		final Story[] as = new Story[l.size()];
		l.toArray(as);
		return as;
	}
	
	public static Iterator<Story> parseFullSearchPartially(final String url, final IFimFictionConnection ffc, final int beginStory) {
		return new Iterator<Story>() {
			{
				i = beginStory / 10;
				loadNext();
			}
			
			private boolean			hasMorePages;
			private Iterator<Story>	cache;
			private int				i;
			
			@Override
			public boolean hasNext() {
				if(cache.hasNext()) {
					return true;
				} else if(hasMorePages) {
					loadNext();
					return cache.hasNext();
				} else {
					return false;
				}
			}
			
			@Override
			public Story next() {
				if(cache.hasNext()) {
					return cache.next();
				} else if(hasMorePages) {
					loadNext();
					return cache.hasNext() ? cache.next() : null;
				} else {
					return null;
				}
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
			private void loadNext() {
				try {
					final Story[] as = parseSearch(Util.getHTML(ffc.getConnection(new URL(url + "&page=" + ++i))));
					hasMorePages = as.length >= 10;
					cache = Arrays.asList(as).iterator();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		};
	}
}
