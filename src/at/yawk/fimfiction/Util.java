package at.yawk.fimfiction;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.htmlparser.Parser;
import org.htmlparser.beans.StringBean;
import org.htmlparser.util.ParserException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

public final class Util {
	public static final String	FIMFICTION	= "http://www.fimfiction.net/";
	public static final File	WORKINGDIR	= new File("test/");
	static {
		WORKINGDIR.mkdirs();
	}
	
	private Util() {
		
	}
	
	public static void copyStream(final InputStream in, final OutputStream out) throws IOException {
		final byte[] buffer = new byte[1024];
		int length;
		while((length = in.read(buffer)) > 0)
			out.write(buffer, 0, length);
	}
	
	/**
	 * Using this in case I need to add a custom user agent or cookies at some
	 * point
	 * 
	 * @throws IOException
	 */
	public static InputStream getURLInputStream(final URL url) throws IOException {
		return getURLConnection(url).getInputStream();
	}
	
	public static URLConnection getURLConnection(final URL url) throws IOException {
		return url.openConnection();
	}
	
	public static Parser getHTML(final URLConnection urlc) throws ParserException {
		return new Parser(urlc);
	}
	
	public static String readFully(final InputStream is) throws IOException {
		final Scanner s = new Scanner(is);
		try {
			final ByteOutputStream bos = new ByteOutputStream();
			copyStream(is, bos);
			return new String(bos.getBytes());
		} finally {
			s.close();
		}
	}
}
