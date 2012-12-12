package at.yawk.fimfiction.examples.backup;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import at.yawk.fimfiction.EnumStoryContentRating;
import at.yawk.fimfiction.SearchRequestBuilder;
import at.yawk.fimfiction.Story;
import at.yawk.fimfiction.Util;
import at.yawk.fimfiction.XMLHelper;
import at.yawk.util.httpserver.HttpServerFactory;
import at.yawk.util.httpserver.IHttpExchange;
import at.yawk.util.httpserver.IHttpHandler;
import at.yawk.util.httpserver.IHttpServer;

import com.thoughtworks.xstream.XStream;

public class Server implements Runnable {
	private final Logger		logger			= Logger.getAnonymousLogger();
	private final static String	LINE_SEPARATOR	= System.getProperty("line.separator");
	private final XStream		xs				= XMLHelper.getXStreamInstance();
	
	private Server() {
		final Handler fh = new ConsoleHandler();
		fh.setFormatter(new Formatter() {
			@Override
			public String format(LogRecord record) {
				return record.getMessage() + LINE_SEPARATOR;
			}
		});
		fh.setLevel(Level.ALL);
		logger.addHandler(fh);
		logger.setUseParentHandlers(false);
	}
	
	public static void main(String[] args) {
		new Server().run();
	}
	
	@Override
	public void run() {
		try {
			final IHttpServer hs = HttpServerFactory.createStandardHTTPServer(new InetSocketAddress(8080));
			hs.createContext("/", new IHttpHandler() {
				@Override
				public void handle(IHttpExchange he) throws IOException {
					try {
						log("Requested " + he.getRequestURI());
						if(he.getRequestURI().toString().equals("/")) {
							String content = "";
							final String post = Util.readFully(he.getRequestBody());
							if(post.trim().length() > 0) {
								final Map<String, String> p = new HashMap<String, String>();
								for(final String s : post.split("&")) {
									final String[] t = s.split("=");
									p.put(t[0], t.length == 1 ? "" : t[1]);
								}
								final SearchRequestBuilder srb = new SearchRequestBuilder();
								srb.setSearchTerm(p.get("searchterm"));
								srb.setMinimumWords(p.get("minimumwords") != null && p.get("minimumwords").matches("[0-9]+") ? Integer.parseInt(p.get("minimumwords")) : null);
								srb.setMaximumWords(p.get("maximumwords") != null && p.get("maximumwords").matches("[0-9]+") ? Integer.parseInt(p.get("maximumwords")) : null);
								srb.setMustBeCompleted(p.containsKey("mustbecompleted"));
								srb.setContentRating(EnumStoryContentRating.parse(p.get("contentrating")));
								final StringBuilder r = new StringBuilder("<table><thead><th>ID</th><th>Name</th><th>Description</th><th>Author</th></thead><tbody>");
								final File mainDir = new File(Main.rootDir, "stories");
								if(mainDir.exists()) {
									for(final File f : mainDir.listFiles(new FileFilter() {
										@Override
										public boolean accept(File arg0) {
											return arg0.isDirectory() && arg0.getName().matches("[0-9]+");
										}
									})) {
										String lastMatchPath = null;
										Story lastMatchStory = null;
										long maxTime = 0;
										for(final File g : f.listFiles(new FileFilter() {
											@Override
											public boolean accept(File arg0) {
												return arg0.isDirectory() && arg0.getName().matches("[0-9]+");
											}
										})) {
											final File s = new File(g, "story.xml");
											if(s.exists()) {
												final Story t = (Story)xs.fromXML(s);
												if(srb.matches(t) && Long.parseLong(g.getName()) > maxTime) {
													maxTime = Long.parseLong(g.getName());
													lastMatchPath = f.getName() + '/' + g.getName();
													lastMatchStory = t;
												}
											}
										}
										if(lastMatchStory != null) {
											r.append("<tr><td><a href=\"");
											r.append("download/");
											r.append(lastMatchPath);
											r.append("\">");
											r.append(lastMatchStory.getId());
											r.append("</a></td><td>");
											r.append(lastMatchStory.getTitle());
											r.append("</td><td>");
											r.append(lastMatchStory.getDescription());
											r.append("</td><td>");
											r.append(lastMatchStory.getAuthor().getName());
											r.append("</td></tr>");
										}
									}
								}
								r.append("</tbody></table>");
								content = r.toString();
							}
							final String response = getPage("index").replace("###CONTENT###", content);
							he.sendResponseHeaders(200, response.length());
							he.getResponseBody().write(response.getBytes());
							he.getResponseBody().close();
						} else {
							he.sendResponseHeaders(404, 0);
						}
					} catch(IOException ioe) {
						log(ioe);
						throw ioe;
					} catch (Exception e) {
						log(e);
					}
				}
			});
			hs.start();
		} catch(IOException e) {
			log(e);
		}
	}
	
	private void log(final Object o) {
		logger.log(Level.INFO, String.valueOf(o));
	}
	
	private void log(final Throwable t) {
		final CharArrayWriter caw = new CharArrayWriter();
		t.printStackTrace(new PrintWriter(caw));
		logger.log(Level.SEVERE, caw.toString());
	}
	
	private String getPage(String name) throws IOException {
		if(!pages.containsKey(name)) {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Util.copyStream(Server.class.getResourceAsStream(name + ".html"), baos);
			pages.put(name, baos.toString());
		}
		return pages.get(name);
	}
	
	private final Map<String, String>	pages	= new HashMap<String, String>();
}
