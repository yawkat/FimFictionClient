package at.yawk.fimfiction;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
				loadNext();
			}
			
			private boolean			hasMorePages;
			private Iterator<Story>	cache;
			private int				i	= beginStory / 10;
			
			@Override
			public boolean hasNext() {
				return cache.hasNext() || hasMorePages;
			}
			
			@Override
			public Story next() {
				if(cache.hasNext()) {
					return cache.next();
				} else if(hasMorePages) {
					loadNext();
					return cache.next();
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
	
	public static String getSearchRequestArguments(String searchTerm, EnumSearchOrder order, EnumMap<EnumCategory, Boolean> categories, EnumStoryContentRating contentRating, EnumStoryMatureCategories matureCategories, boolean completed, Integer minimumWords, Integer maximumWords, EnumMap<EnumCharacter, Boolean> characters) {
		final Map<String, String> m = new HashMap<String, String>();
		m.put("view", "category");
		m.put("search", searchTerm);
		m.put("order", order.getSearchValue());
		for(final EnumCategory ec : EnumCategory.values()) {
			final Boolean b = categories.get(ec);
			m.put(ec.getSearchValue(), b == null ? "" : b.booleanValue() ? "1" : "2");
		}
		m.put("content_rating", Integer.toString(contentRating.getSearchId()));
		m.put("mature_categories", Integer.toString(matureCategories.getSearchId()));
		if(completed)
			m.put("completed", "1");
		m.put("minimum_words", minimumWords == null ? "" : minimumWords.toString());
		m.put("maximum_words", maximumWords == null ? "" : maximumWords.toString());
		final StringBuilder sb = new StringBuilder();
		for(final Entry<String, String> e : m.entrySet()) {
			sb.append(e.getKey());
			sb.append('=');
			sb.append(e.getValue());
			sb.append('&');
		}
		for(final EnumCharacter ec : EnumCharacter.values()) {
			final Boolean b = characters.get(ec);
			if(b != null) {
				sb.append(b.booleanValue() ? "characters[]" : "characters_execluded[]");
				sb.append('=');
				sb.append(ec.getId());
				sb.append('&');
			}
		}
		return sb.toString().substring(0, sb.length() - 1);
	}
}
