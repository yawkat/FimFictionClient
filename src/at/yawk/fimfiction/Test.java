package at.yawk.fimfiction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.util.ParserException;
import org.xml.sax.SAXException;

public class Test {
	
	private static final Set<Story>	stories	= new HashSet<>();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws ParserException, MalformedURLException, IOException {
		Stories.updateStory(new Story(6635));
		// System.out.println(Util.readFully(Util.getURLInputStream(new
		// URL("http://www.fimfiction.net/story/6635"))));
		System.out.println(Util.getHTML(Util.getURLConnection(new URL("http://www.fimfiction.net/story/6635"))).getEncoding());
	}
	
}
