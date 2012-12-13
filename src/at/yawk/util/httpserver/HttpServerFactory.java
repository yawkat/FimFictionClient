package at.yawk.util.httpserver;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpServerFactory {
	
	public static IHttpServer createStandardHTTPServer(InetSocketAddress address) throws IOException {
		// return new SunHttpServer(address);
		return new SocketHttpServer(address.getPort());
	}
}
