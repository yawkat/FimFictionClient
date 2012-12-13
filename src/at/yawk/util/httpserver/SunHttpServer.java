package at.yawk.util.httpserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class SunHttpServer implements IHttpServer {
	private final HttpServer httpserver;
	
	public SunHttpServer(HttpServer server) {
		this.httpserver = server;
	}
	
	public SunHttpServer(InetSocketAddress address) throws IOException {
		this(HttpServer.create(address, 0));
	}

	@Override
	public void createContext(String string, final IHttpHandler httpHandler) {
		httpserver.createContext(string, new HttpHandler() {
			@Override
			public void handle(final HttpExchange arg0) throws IOException {
				httpHandler.handle(new IHttpExchange() {
					
					@Override
					public void close() throws IOException {
						arg0.close();
					}
					
					@Override
					public void sendResponseHeaders(int i, long j) throws IOException {
						arg0.sendResponseHeaders(i, j);
					}
					
					@Override
					public OutputStream getResponseBody() {
						return arg0.getResponseBody();
					}
					
					@Override
					public URI getRequestURI() {
						return arg0.getRequestURI();
					}

					@Override
					public InputStream getRequestBody() {
						return arg0.getRequestBody();
					}
				});
			}
		});
	}

	@Override
	public void start() {
		httpserver.start();
	}

	@Override
	public void stop(int i) {
		httpserver.stop(i);
	}
}
