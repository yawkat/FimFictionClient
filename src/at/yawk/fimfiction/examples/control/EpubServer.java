package at.yawk.fimfiction.examples.control;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class EpubServer {
	private final DownloadManager	dlManager;
	private HttpServer				hs;
	private int						port				= 8080;
	private boolean					useCustomStylesheet	= true;
	
	public EpubServer(final DownloadManager dlManager) {
		this.dlManager = dlManager;
	}
	
	public void start() {
		try {
			hs = HttpServer.create(new InetSocketAddress(port), 0);
			hs.createContext("/", new HttpHandler() {
				@Override
				public void handle(HttpExchange arg0) throws IOException {
					String loc = arg0.getRequestURI().toString().substring(1);
					loc = loc.substring(0, loc.indexOf('/')).replace("%20", " ");
					final File f = new File(dlManager.getDownloadDirectory(), loc);
					if(!f.exists())
						arg0.sendResponseHeaders(404, -1);
					else {
						try {
							final ZipFile zf = new ZipFile(f);
							final String name = arg0.getRequestURI().toString().substring(arg0.getRequestURI().toString().substring(1).indexOf('/') + 2).trim();
							ZipEntry ze = zf.getEntry(name);
							if(ze == null) {
								arg0.sendResponseHeaders(404, -1);
							} else {
								final InputStream is = zf.getInputStream(ze);
								if(!useCustomStylesheet || (!name.endsWith(".html") && !name.endsWith(".htm"))) {
									arg0.sendResponseHeaders(200, ze.getSize());
									final byte[] buf = new byte[1024];
									int length;
									while((length = is.read(buf)) > 0)
										arg0.getResponseBody().write(buf, 0, length);
								} else {
									final Document d = Jsoup.parse(is, "UTF-8", "/");
									final Elements e = d.getElementsByTag("head");
									if(e.size() > 0) {
										final Element e1 = e.first();
										final Element style = new Element(Tag.valueOf("style"), "/");
										style.attr("type", "text/css");
										style.appendText("body{max-width:60em;margin:auto}p{font-size:1.2em!important;font-family:Verdana,Arial,sans-serif!important}");
										e1.appendChild(style);
									}
									arg0.sendResponseHeaders(200, d.toString().getBytes().length);
									arg0.getResponseBody().write(d.toString().getBytes());
								}
								arg0.close();
							}
							zf.close();
						} catch(Exception e) {
							e.printStackTrace();
							arg0.sendResponseHeaders(500, -1);
						}
					}
				}
			});
			hs.start();
		} catch(IOException e2) {
			e2.printStackTrace();
		}
		
	}
	
	public void stop() {
		hs.stop(0);
		hs = null;
	}
	
	public boolean running() {
		return hs != null;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public int getPort() {
		return port;
	}
	
	public boolean useCustomStylesheet() {
		return useCustomStylesheet;
	}
	
	public void setUseCustomStylesheet(final boolean useCustomStylesheet) {
		this.useCustomStylesheet = useCustomStylesheet;
	}
}
