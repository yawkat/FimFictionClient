package at.yawk.fimfiction.examples.control.proxy.client;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import at.yawk.fimfiction.IURLConnection;
import at.yawk.fimfiction.IWebProvider;

public class ProxyWebProvider implements IWebProvider {
	private final String	host;
	private final int		port;
	
	public ProxyWebProvider(final String host, final int port) {
		this.host = host;
		this.port = port;
	}
	
	@Override
	public IURLConnection getConnection(final URL url) throws IOException {
		return new IURLConnection() {
			private final Socket			proxySocket;
			private final DataInputStream	in;
			private final DataOutputStream	out;
			private boolean					hasConnected	= false;
			private boolean					hasDoneWriting	= false;
			private final List<String[]>	headers			= new ArrayList<String[]>();
			private long					contentLength;
			private ByteArrayOutputStream	outputCache		= new ByteArrayOutputStream();
			{
				proxySocket = new Socket(host, port);
				in = new DataInputStream(proxySocket.getInputStream());
				out = new DataOutputStream(proxySocket.getOutputStream());
				out.writeUTF(url.toString());
			}
			
			@Override
			public void setHeader(String name, String value) {
				if(!hasConnected) {
					try {
						out.write(1);
						out.writeUTF(name);
						out.writeUTF(value);
						out.flush();
					} catch(IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			@Override
			public URL getURL() {
				return url;
			}
			
			@Override
			public OutputStream getOutputStream() throws IOException {
				return hasConnected && !hasDoneWriting ? outputCache : null;
			}
			
			@Override
			public InputStream getInputStream() throws IOException {
				return new InputStream() {
					
					@Override
					public int read() throws IOException {
						tryReceive();
						return in.read();
					}
				};
			}
			
			@Override
			public String[] getHeader(int i) {
				try {
					tryReceive();
				} catch(IOException e) {
					e.printStackTrace();
				}
				return headers.size() > i ? headers.get(i) : new String[] { null, null };
			}
			
			@Override
			public long getContentLengthLong() {
				try {
					tryReceive();
				} catch(IOException e) {
					e.printStackTrace();
				}
				return hasConnected ? contentLength : -1;
			}
			
			private void tryReceive() throws IOException {
				connect();
				if(!hasDoneWriting) {
					out.writeInt(outputCache.size());
					out.write(outputCache.toByteArray());
					outputCache.reset();
					contentLength = in.readLong();
					final int headerCount = in.readInt();
					for(int i = 0; i < headerCount; i++) {
						final String[] header = new String[] { in.readUTF(), in.readUTF() };
						headers.add(header);
					}
					hasDoneWriting = true;
				}
			}
			
			@Override
			public void connect() throws IOException {
				if(!hasConnected) {
					out.write(0);
					out.flush();
					hasConnected = true;
				}
			}
		};
	}
	
}
