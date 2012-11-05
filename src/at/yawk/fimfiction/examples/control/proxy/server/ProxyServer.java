package at.yawk.fimfiction.examples.control.proxy.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

/**
 * Sorry about the messy coding, this is more for my own special purposes rather
 * than common usage.
 * 
 * @author Jonas Konrad
 * 
 */
public class ProxyServer implements Runnable {
	public static void main(String[] args) {
		new ProxyServer().run();
	}
	
	@Override
	public void run() {
		try {
			final ServerSocket ss = new ServerSocket(6666);
			try {
				System.out.println("Waiting for connections");
				while(true) {
					final Socket s = ss.accept();
					System.out.println("Received connection");
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								final DataInputStream dis = new DataInputStream(s.getInputStream());
								final DataOutputStream dos = new DataOutputStream(s.getOutputStream());
								try {
									final String host = dis.readUTF();
									System.out.println("Preparing for " + host);
									final URLConnection urlc = new URL(host).openConnection();
									urlc.setDoInput(true);
									urlc.setDoOutput(true);
									urlc.setUseCaches(false);
									
									System.out.println("Waiting for request properties");
									propertyLoop: while(true) {
										final int packetId = dis.read();
										switch(packetId) {
										case 0: // Connect
											break propertyLoop;
										case 1: // Set header
											final String key = dis.readUTF();
											final String value = dis.readUTF();
											urlc.setRequestProperty(key, value);
											break;
										}
									}
									
									System.out.println("Connecting");
									urlc.connect();
									
									System.out.println("Writing POST");
									// copy data (client >> web server)
									final int contentLength = dis.readInt();
									for(int i = 0; i < contentLength; i++)
										urlc.getOutputStream().write(dis.read());
									
									System.out.println("Sending content length");
									// Send content length (I know I could just
									// use
									// the header but I am too lazy)
									dos.writeLong(urlc.getContentLengthLong());
									
									System.out.println("Sending headers");
									// Send headers
									int headerCount = 1;
									for(; urlc.getHeaderFieldKey(headerCount) != null; headerCount++)
										;
									dos.writeInt(headerCount - 1);
									for(int i = 1; i < headerCount; i++) {
										dos.writeUTF(urlc.getHeaderFieldKey(i));
										dos.writeUTF(urlc.getHeaderField(i));
									}
									
									System.out.println("Sending body");
									// copy data (web server >> client)
									final byte[] buf = new byte[1024];
									int len;
									while((len = urlc.getInputStream().read(buf)) > 0)
										dos.write(buf, 0, len);
								} finally {
									dis.close();
									dos.close();
									s.close();
								}
								System.out.println("Done!");
							} catch(Exception e) {
								e.printStackTrace();
							}
						}
					}).start();
				}
			} finally {
				ss.close();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
