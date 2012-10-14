package at.yawk.fimfiction;

import com.thoughtworks.xstream.XStream;

public class XMLHelper {
	public static XStream getXStreamInstance() {
		final XStream xs = new XStream();
		xs.alias("story", Story.class);
		xs.alias("chapter", Chapter.class);
		xs.alias("author", Author.class);
		return xs;
	}
}
