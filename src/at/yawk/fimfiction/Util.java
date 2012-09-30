package at.yawk.fimfiction;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

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
		return url.openStream();
	}
}
