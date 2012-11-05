package at.yawk.fimfiction;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class StandardUrlConnection implements IURLConnection {
	private final URLConnection	urlc;
	
	public StandardUrlConnection(final URLConnection urlc) {
		this.urlc = urlc;
	}
	
	@Override
	public InputStream getInputStream() throws IOException {
		return urlc.getInputStream();
	}
	
	@Override
	public URL getURL() {
		return urlc.getURL();
	}
	
	@Override
	public void connect() throws IOException {
		urlc.connect();
	}
	
	@Override
	public OutputStream getOutputStream() throws IOException {
		return urlc.getOutputStream();
	}
	
	@Override
	public long getContentLengthLong() {
		return urlc.getContentLengthLong();
	}
	
	@Override
	public void setHeader(String name, String value) {
		urlc.setRequestProperty(name, value);
	}
	
	@Override
	public String[] getHeader(int i) {
		return new String[] { urlc.getHeaderFieldKey(i), urlc.getHeaderField(i) };
	}
}
