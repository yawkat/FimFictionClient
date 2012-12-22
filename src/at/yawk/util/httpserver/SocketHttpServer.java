package at.yawk.util.httpserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class SocketHttpServer implements IHttpServer {
	private final ServerSocket			socket;
	private boolean						listen		= false;
	
	private final Thread				listenThread;
	
	private Map<String, IHttpHandler>	handlers	= new HashMap<String, IHttpHandler>();
	
	public SocketHttpServer(int port) throws IOException {
		socket = new ServerSocket(port);
		listenThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(!socket.isClosed()) {
					try {
						final Socket s = socket.accept();
						if(listen) {
							readUntil(s.getInputStream(), ' ', 8);
							final String uri = new String(readUntil(s.getInputStream(), ' ', 200));
							while(readUntil(s.getInputStream(), '\n', 200).length > 1)
								;
							final IHttpExchange i = new IHttpExchange() {
								@Override
								public void close() throws IOException {
									s.close();
								}
								
								@Override
								public void sendResponseHeaders(int i, long j) throws IOException {
									String res;
									switch(i) {
									case 200:
										res = "HTTP/1.1 " + i + " OK\r\nServer: Yawkat Java HTTP server\r\nContent-Length: " + j + "\r\nContent-Language: en\r\nConnection: close\r\n\r\n";
										break;
									case 404:
										res = "HTTP/1.1 404 Not Found\r\nServer: Yawkat Java HTTP server\r\nContent-Length: " + j + "\r\nContent-Language: en\r\nConnection: close\r\n\r\n";
										break;
									default:
										throw new IOException("Unknown statuscode " + i);
									}
									getResponseBody().write(res.getBytes());
								}
								
								@Override
								public OutputStream getResponseBody() {
									try {
										return s.getOutputStream();
									} catch(IOException e) {
										e.printStackTrace();
									}
									return null;
								}
								
								@Override
								public URI getRequestURI() {
									try {
										return new URI(uri);
									} catch(URISyntaxException e) {
										e.printStackTrace();
									}
									return null;
								}
								
								@Override
								public InputStream getRequestBody() {
									try {
										return s.getInputStream();
									} catch(IOException e) {
										e.printStackTrace();
									}
									return null;
								}
							};
							for(String s1 : handlers.keySet()) {
								if(uri.startsWith(s1)) {
									handlers.get(s1).handle(i);
									break;
								}
							}
							if(!s.isClosed())
								s.close();
						}
					} catch(IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		listenThread.start();
	}
	
	@Override
	public void createContext(String string, IHttpHandler httpHandler) {
		handlers.put(string, httpHandler);
	}
	
	@Override
	public void start() {
		listen = true;
	}
	
	@Override
	public void stop(int i) {
		listen = false;
	}
	
	private byte[] readUntil(InputStream is, char c, int j) throws IOException {
		final ByteArrayOutputStream s = new ByteArrayOutputStream();
		int i;
		while((i = is.read()) != c && j-- > 0)
			s.write(i);
		return s.toByteArray();
	}
}
