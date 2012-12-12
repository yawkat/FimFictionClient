package at.yawk.util.httpserver;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

public interface IHttpExchange extends Closeable {

	URI getRequestURI();

	void sendResponseHeaders(int i, long j) throws IOException;

	OutputStream getResponseBody();

	InputStream getRequestBody();
}
