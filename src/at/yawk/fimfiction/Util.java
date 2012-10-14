package at.yawk.fimfiction;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

public final class Util {
	public static final String	FIMFICTION	= "http://www.fimfiction.net/";
	
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
	public static InputStream getURLInputStream(final URL url, final IFimFictionConnection ffc) throws IOException {
		return ffc.getConnection(url).getInputStream();
	}
	
	public static Document getHTML(final URLConnection urlc) throws IOException {
		return Jsoup.parse(readFully(urlc.getInputStream()), urlc.getURL().toString());
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
