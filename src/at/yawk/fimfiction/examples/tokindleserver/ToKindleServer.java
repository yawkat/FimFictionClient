package at.yawk.fimfiction.examples.tokindleserver;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.json.JSONException;
import org.json.JSONWriter;
import at.yawk.fimfiction.EnumDownloadType;
import at.yawk.fimfiction.FimFictionConnectionAccount;
import at.yawk.fimfiction.FimFictionConnectionStandard;
import at.yawk.fimfiction.SearchRequestBuilder;
import at.yawk.fimfiction.Searches;
import at.yawk.fimfiction.Stories;
import at.yawk.fimfiction.Story;
import at.yawk.fimfiction.Util;
import at.yawk.util.httpserver.HttpServerFactory;
import at.yawk.util.httpserver.IHttpExchange;
import at.yawk.util.httpserver.IHttpHandler;
import at.yawk.util.httpserver.IHttpServer;

public class ToKindleServer {
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException {
		final FimFictionConnectionAccount connection = new FimFictionConnectionAccount();
		connection.setDisplayMature(false);
		final AtomicLong lastReconnect = new AtomicLong();
		
		final IHttpServer server = HttpServerFactory.createStandardHTTPServer(new InetSocketAddress(9898));
		server.createContext("/", new IHttpHandler() {
			@Override
			public void handle(IHttpExchange ex) throws IOException {
				if(lastReconnect.get() < System.currentTimeMillis() - 60 * 60 * 1000) {
					lastReconnect.set(System.currentTimeMillis());
					System.out.println("Refreshing connection...");
					connection.login(args[0], args[1]);
				}
				System.out.println("Requested " + ex.getRequestURI());
				if(ex.getRequestURI().toASCIIString().startsWith("/tokindle/")) {
					String storyID = ex.getRequestURI().toASCIIString().substring(ex.getRequestURI().toASCIIString().lastIndexOf('/') + 1);
					if(storyID.contains("?"))
						storyID = storyID.substring(0, storyID.indexOf('?'));
					final boolean isUsingSMTPAuth = args.length > 5;
					final boolean isUsingEmailAsUser = args.length == 6;
					final String smtpAdress = args[4];
					final String smtpHost = smtpAdress.contains(":") ? smtpAdress.substring(0, smtpAdress.indexOf(':')) : smtpAdress;
					final String smtpPort = smtpAdress.contains(":") ? smtpAdress.substring(smtpAdress.indexOf(':') + 1) : "587";
					final String from = args[2];
					final String to = args[3];
					final String smtpUsername = isUsingSMTPAuth ? isUsingEmailAsUser ? from : args[5] : null;
					final String smtpPassword = isUsingSMTPAuth ? args[isUsingEmailAsUser ? 5 : 6] : null;
					
					System.out.println("Sending story " + storyID + " from " + from + " to " + to + " using SMTP server at " + smtpHost + ":" + smtpPort + (isUsingSMTPAuth ? " using username " + smtpUsername + " and password " + smtpPassword : ""));
					
					boolean b = false;
					Exception f = null;
					try {
						final Story s = new Story(Integer.parseInt(storyID));
						final File inputFile = File.createTempFile("convert_book", ".epub");
						final File outputFile = File.createTempFile("convert_book", ".mobi");
						
						System.out.println("Downloading story as .EPUB...");
						Stories.downloadStory(s, new FileOutputStream(inputFile), EnumDownloadType.EPUB, new FimFictionConnectionStandard());
						
						System.out.println("Converting to .MOBI using ebook-convert...");
						final Process p;
						try {
							p = Runtime.getRuntime().exec("ebook-convert \"" + inputFile.getAbsolutePath() + "\" \"" + outputFile.getAbsolutePath() + "\"");
						} catch(IOException ioe) {
							System.out.println("ebook-convert could not be found (Is calibre installed?)");
							return;
						}
						p.waitFor();
						
						System.out.println("Deleting .EPUB...");
						inputFile.delete();
						
						System.out.println("Initializing SMTP connection...");
						final Session session;
						if(isUsingSMTPAuth) {
							final Properties props = new Properties();
							props.put("mail.smtp.auth", "true");
							props.put("mail.smtp.starttls.enable", "true");
							props.put("mail.smtp.host", smtpHost);
							props.put("mail.smtp.port", smtpPort);
							session = Session.getInstance(props, new javax.mail.Authenticator() {
								protected PasswordAuthentication getPasswordAuthentication() {
									return new PasswordAuthentication(smtpUsername, smtpPassword);
								}
							});
						} else {
							final Properties props = new Properties();
							props.put("mail.smtp.host", smtpHost);
							props.put("mail.smtp.port", smtpPort);
							session = Session.getInstance(props);
						}
						
						System.out.println("Composing e-mail...");
						final Message message = new MimeMessage(session);
						message.setFrom(new InternetAddress(from));
						message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
						message.setSubject("e-book");
						
						final Multipart multipart = new MimeMultipart();
						
						final BodyPart body = new MimeBodyPart();
						body.setText("Story " + s.getId() + " from FimFiction.net");
						multipart.addBodyPart(body);
						
						final BodyPart attachment = new MimeBodyPart();
						attachment.setDataHandler(new DataHandler(new FileDataSource(outputFile)));
						attachment.setFileName("ebook.mobi");
						multipart.addBodyPart(attachment);
						
						message.setContent(multipart);
						
						System.out.println("Sending e-mail...");
						Transport.send(message);
						
						System.out.println("Deleting .MOBI...");
						outputFile.delete();
						
						b = true;
					} catch(Exception e) {
						e.printStackTrace();
						f = e;
					}
					final ByteArrayOutputStream out = new ByteArrayOutputStream();
					final Writer w = new OutputStreamWriter(out);
					final JSONWriter json = new JSONWriter(w);
					try {
						json.object();
						json.key("status");
						json.value(b ? 1 : 0);
						json.key("displayvalue");
						json.value(b ? "success" : "error");
						if(!b) {
							json.key("error");
							json.value(f.toString());
						}
						json.endObject();
					} catch(JSONException j) {
						j.printStackTrace();
					}
					w.flush();
					ex.sendResponseHeaders(200, out.size());
					ex.getResponseBody().write(out.toByteArray());
					ex.close();
				} else if(ex.getRequestURI().toASCIIString().startsWith("/list")) {
					final ByteArrayOutputStream out = new ByteArrayOutputStream();
					final Writer w = new OutputStreamWriter(out);
					final JSONWriter json = new JSONWriter(w);
					try {
						json.object();
						{
							json.key("readlater");
							json.array();
							final Iterator<Story> i = Searches.parseFullSearchPartially(Util.FIMFICTION + "index.php?" + new SearchRequestBuilder().setMustBeReadLater(true), connection, 0);
							while(i.hasNext()) {
								final Story s = i.next();
								Stories.updateStory(s, connection);
								json.object();
								json.key("id");
								json.value(s.getId());
								json.key("title");
								json.value(s.getTitle());
								json.endObject();
							}
							json.endArray();
						}
						{
							json.key("favorites");
							json.array();
							final Iterator<Story> i = Searches.parseFullSearchPartially(Util.FIMFICTION + "index.php?" + new SearchRequestBuilder().setMustBeUnread(true).setMustBeFavorite(true), connection, 0);
							while(i.hasNext()) {
								final Story s = i.next();
								Stories.updateStory(s, connection);
								json.object();
								json.key("id");
								json.value(s.getId());
								json.key("title");
								json.value(s.getTitle());
								json.endObject();
							}
							json.endArray();
						}
						json.endObject();
					} catch(JSONException j) {
						j.printStackTrace();
					}
					w.flush();
					ex.sendResponseHeaders(200, out.size());
					ex.getResponseBody().write(out.toByteArray());
					ex.close();
				}
			}
		});
		server.start();
	}
}
