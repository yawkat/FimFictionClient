package at.yawk.util.httpserver;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

public class HttpServerFactory {
	public static IHttpServer createStandardHTTPServer(InetSocketAddress address) throws IOException {
		return new SunHttpServer(HttpServer.create(address, 0));
	}
}
