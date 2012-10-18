package at.yawk.fimfiction.examples.kindledonwload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import at.yawk.fimfiction.EnumDownloadType;
import at.yawk.fimfiction.FimFictionConnectionStandard;
import at.yawk.fimfiction.Stories;
import at.yawk.fimfiction.Story;

public final class Main {
	private Main() {
		
	}
	
	public static void main(final String[] args) {
		if(args.length >= 4 && args.length <= 6) {
			final String storyID = args[0];
			final boolean isUsingSMTPAuth = args.length > 4;
			final boolean isUsingEmailAsUser = args.length == 5;
			final String smtpAdress = args[3];
			final String smtpHost = smtpAdress.contains(":") ? smtpAdress.substring(0, smtpAdress.indexOf(':')) : smtpAdress;
			final String smtpPort = smtpAdress.contains(":") ? smtpAdress.substring(smtpAdress.indexOf(':') + 1) : "587";
			final String from = args[1];
			final String to = args[2];
			final String smtpUsername = isUsingSMTPAuth ? isUsingEmailAsUser ? from : args[4] : null;
			final String smtpPassword = isUsingSMTPAuth ? args[isUsingEmailAsUser ? 4 : 5] : null;
			
			System.out.println("Sending story " + storyID + " from " + from + " to " + to + " using SMTP server at " + smtpHost + ":" + smtpPort + (isUsingSMTPAuth ? " using username " + smtpUsername + " and password " + smtpPassword : ""));
			
			final Story s;
			try {
				s = new Story(Integer.parseInt(storyID));
			} catch(NumberFormatException nfe) {
				System.out.println("Invalid Book ID: " + storyID);
				return;
			}
			try {
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
				
				System.out.println("Done!");
			} catch(IOException e) {
				e.printStackTrace();
			} catch(InterruptedException e) {
				e.printStackTrace();
			} catch(AddressException e) {
				e.printStackTrace();
			} catch(MessagingException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Incorrect usage. <java> <Story ID> <From e-mail adress> <To e-mail adress> <SMTP host> [[<SMTP username>] <SMTP password>]");
		}
	}
}
