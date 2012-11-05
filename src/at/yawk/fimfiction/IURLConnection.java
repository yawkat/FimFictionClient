package at.yawk.fimfiction;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public interface IURLConnection {
	InputStream getInputStream() throws IOException;
	
	URL getURL();
	
	void connect() throws IOException;
	
	OutputStream getOutputStream() throws IOException;
	
	long getContentLengthLong();
	
	void setHeader(String name, String value);
}
