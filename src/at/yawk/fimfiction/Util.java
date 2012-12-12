package at.yawk.fimfiction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import at.yawk.fimfiction.examples.control.IDownloadUpdate;

public final class Util {
	public static final String	FIMFICTION	= "http://www.fimfiction.net/";
	
	private Util() {
		
	}
	
	public static void copyStream(final InputStream in, final OutputStream out) throws IOException {
		copyStream(in, out, null, 0);
	}
	
	public static void copyStream(InputStream in, OutputStream out, IDownloadUpdate update, long streamLength) throws IOException {
		final byte[] buffer = new byte[1024];
		int length;
		int counter = 0;
		while((length = in.read(buffer)) > 0) {
			out.write(buffer, 0, length);
			if(update != null)
				update.setProgress((float)((double)(counter++ * buffer.length + length) / streamLength));
		}
		if(update != null)
			update.setProgress(1);
	}
	
	public static Document getHTML(final IURLConnection iurlConnection) throws IOException {
		return Jsoup.parse(readFully(iurlConnection.getInputStream()), iurlConnection.getURL().toString());
	}
	
	public static String readFully(final InputStream is) throws IOException {
		final Scanner s = new Scanner(is);
		try {
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			copyStream(is, bos);
			return bos.toString();
		} finally {
			s.close();
		}
	}
	
	public static void clearStream(final InputStream is) throws IOException {
		while(is.read() >= 0)
			;
	}
}
