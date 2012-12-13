package at.yawk.fimfiction.examples.sqlitedl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONException;

import at.yawk.fimfiction.Chapter;
import at.yawk.fimfiction.EnumDownloadType;
import at.yawk.fimfiction.EnumSearchOrder;
import at.yawk.fimfiction.FimFictionConnectionStandard;
import at.yawk.fimfiction.IFimFictionConnection;
import at.yawk.fimfiction.SearchRequestBuilder;
import at.yawk.fimfiction.Searches;
import at.yawk.fimfiction.Stories;
import at.yawk.fimfiction.Story;
import at.yawk.fimfiction.Util;

@SuppressWarnings("unused")
public class SQLiteDownloader implements Runnable {
	public static void main(String[] args) {
		new SQLiteDownloader().run();
	}
	
	private Connection					databaseConnection;
	private final Lock					connectionLock			= new ReentrantLock();
	private final Object				getLock					= new Object();
	
	private final File					storyDownloadDirectory	= new File("data");
	private final IFimFictionConnection	connection				= new FimFictionConnectionStandard();
	
	private SQLiteDownloader() {
		storyDownloadDirectory.mkdirs();
	}
	
	private void createTables() throws SQLException {
		databaseConnection.createStatement().execute("CREATE TABLE IF NOT EXISTS authors (id INTEGER PRIMARY KEY, authorid int(16), name varchar(100))");
		databaseConnection.createStatement().execute("CREATE TABLE IF NOT EXISTS chapters (id INTEGER PRIMARY KEY AUTOINCREMENT, updateid int(10), chapterid int(10), storyindex int(4), title varchar(100), words int(10), views int(10), modifytime date);");
		databaseConnection.createStatement().execute("CREATE TABLE IF NOT EXISTS stories (id INTEGER PRIMARY KEY AUTOINCREMENT, storyid int(8), description varchar(10000), shortdescription varchar(10000), title varchar(100), modifytime date, imagelocation varchar(300), fullimagelocation varchar(300), views int(6), totalviews int(6), comments int(6), authorid int(16), status varchar(30), contentrating varchar(30), likes int(6), dislikes int(6), words int(6), filename varchar(32));");
	}
	
	@Override
	public void run() {
		System.out.println("Starting");
		connect();
		autoDisconnect();
		
		try {
			createTables();
		} catch(SQLException e1) {
			e1.printStackTrace();
		}
		continueUpdateStories();
		
		System.out.println("Stopping");
		System.exit(0);
	}
	
	private void connect() {
		try {
			System.out.println("Loading SQL driver");
			Class.forName("org.sqlite.JDBC");
			System.out.println("Connecting");
			databaseConnection = DriverManager.getConnection("jdbc:sqlite:fimfiction.db");
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void autoDisconnect() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				disconnect();
			}
		}));
	}
	
	private void disconnect() {
		try {
			System.out.println("Disconnecting");
			databaseConnection.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	private File getNewRandomEpubFile() {
		File f;
		do {
			f = new File(storyDownloadDirectory, RandomStringUtils.randomAlphanumeric(16) + "." + EnumDownloadType.EPUB.getFileType());
		} while(f.exists());
		return f;
	}
	
	private void updateStory(Story s) throws IOException, JSONException, SQLException {
		if(Stories.updateStory(s, connection)) {
			final ResultSet result = databaseConnection.createStatement().executeQuery("SELECT * FROM stories WHERE storyid = '" + s.getId() + "'");
			final int requestLength;
			{
				final ResultSet rezLen = databaseConnection.createStatement().executeQuery("SELECT count(*) FROM stories WHERE storyid = '" + s.getId() + "'");
				requestLength = rezLen.getInt(1);
			}
			
			final boolean existsAlready = requestLength > 0;
			for(int i = 1; i < requestLength; i++)
				result.next();
			final boolean outdatedEntry = existsAlready && result.getDate("modifytime").getTime() != s.getModifyTime().getTime();
			if(outdatedEntry || !existsAlready) {
				System.out.println("Updating story " + s.getId() + " (" + s.getTitle().replace("\n", "") + ")");
				final File epub = getNewRandomEpubFile();
				final OutputStream os = new FileOutputStream(epub);
				Stories.downloadStory(s, os, EnumDownloadType.EPUB, connection);
				os.close();
				
				final PreparedStatement prepared = databaseConnection.prepareStatement("INSERT INTO stories (storyid, description, shortdescription, title, modifytime, imagelocation, fullimagelocation, views, totalviews, comments, authorid, status, contentrating, likes, dislikes, words, filename) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
				int index = 1;
				prepared.setInt(index++, s.getId());
				prepared.setString(index++, s.getDescription());
				prepared.setString(index++, s.getShortDescription());
				prepared.setString(index++, s.getTitle());
				prepared.setDate(index++, new Date(s.getModifyTime().getTime()));
				prepared.setString(index++, s.getImageLocation());
				prepared.setString(index++, s.getFullImageLocation());
				prepared.setInt(index++, s.getViews());
				prepared.setInt(index++, s.getTotalViews());
				prepared.setInt(index++, s.getComments());
				prepared.setInt(index++, s.getAuthor().getId());
				prepared.setString(index++, s.getStatus().toString());
				prepared.setString(index++, s.getContentRating().toString());
				prepared.setInt(index++, s.getLikes());
				prepared.setInt(index++, s.getDislikes());
				prepared.setInt(index++, s.getWords());
				prepared.setString(index++, epub.getName());
				
				final ResultSet generatedKeys;
				connectionLock.lock();
				try {
					prepared.execute();
					generatedKeys = prepared.getGeneratedKeys();
				} finally {
					connectionLock.unlock();
				}
				final int totalId = generatedKeys.getInt(1);
				for(Chapter c : s.getChapters()) {
					final PreparedStatement cprepared = databaseConnection.prepareStatement("INSERT INTO chapters (updateid, chapterid, storyindex, title, words, views, modifytime) VALUES (?, ?, ?, ?, ?, ?, ?)");
					index = 1;
					cprepared.setInt(index++, totalId);
					cprepared.setInt(index++, c.getId());
					cprepared.setInt(index++, c.getStoryIndex());
					cprepared.setString(index++, c.getTitle());
					cprepared.setInt(index++, c.getWords());
					cprepared.setInt(index++, c.getViews());
					cprepared.setDate(index++, new Date(c.getModifyTime().getTime()));
					cprepared.execute();
				}
				
				final int authorLength;
				{
					final ResultSet rezLen = databaseConnection.createStatement().executeQuery("SELECT count(*) FROM authors WHERE authorid = '" + s.getAuthor().getId() + "'");
					authorLength = rezLen.getInt(1);
				}
				final ResultSet authorGetRequest = databaseConnection.createStatement().executeQuery("SELECT * FROM authors WHERE authorid = '" + s.getAuthor().getId() + "'");
				final boolean insertAuthor;
				if(authorLength > 0) {
					for(int i = 1; i < authorLength; i++)
						authorGetRequest.next();
					insertAuthor = !authorGetRequest.getString("name").equals(s.getAuthor().getName());
				} else {
					insertAuthor = true;
				}
				if(insertAuthor) {
					final PreparedStatement aprepared = databaseConnection.prepareStatement("INSERT INTO authors (authorid, name) VALUES (?, ?)");
					aprepared.setInt(1, s.getAuthor().getId());
					aprepared.setString(2, s.getAuthor().getName());
					aprepared.execute();
				}
			}
		}
	}
	
	private void truncateDB() throws SQLException {
		databaseConnection.createStatement().execute("DELETE FROM stories");
		databaseConnection.createStatement().execute("DELETE FROM chapters");
		databaseConnection.createStatement().execute("DELETE FROM authors");
	}
	
	private void continueUpdateStories() {
		final int maxId = Searches.parseFullSearchPartially(Util.FIMFICTION + "index.php?" + new SearchRequestBuilder().setSearchOrder(EnumSearchOrder.FIRST_POSTED_DATE), connection, 0).next().getId() + 1000;
		
		final Executor ex = Executors.newFixedThreadPool(4);
		final AtomicInteger ai = new AtomicInteger();
		for(int i = maxId; i > 0; i--) {
			final int index = i;
			ai.incrementAndGet();
			ex.execute(new Runnable() {
				@Override
				public void run() {
					try {
						updateStory(new Story(index));
					} catch(Exception e) {
						e.printStackTrace();
					} finally {
						ai.decrementAndGet();
					}
				}
			});
		}
		
		while(ai.get() > 0) {
			try {
				TimeUnit.SECONDS.sleep(2);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void dumpDatabase(final boolean dumpEpub, final OutputStream os) throws SQLException, IOException {
		System.out.println("Dumping");
		final DataOutputStream out = new DataOutputStream(os);
		final ResultSet stories = databaseConnection.createStatement().executeQuery("SELECT * FROM stories");
		while(stories.next()) {
			out.write(1);
			out.writeInt(stories.getInt("id"));
			out.writeInt(stories.getInt("storyid"));
			out.writeUTF(nullToEmptyString(stories.getString("description")));
			out.writeUTF(nullToEmptyString(stories.getString("shortdescription")));
			out.writeUTF(nullToEmptyString(stories.getString("title")));
			out.writeLong(stories.getDate("modifytime").getTime());
			out.writeUTF(nullToEmptyString(stories.getString("imagelocation")));
			out.writeUTF(nullToEmptyString(stories.getString("fullimagelocation")));
			out.writeInt(stories.getInt("views"));
			out.writeInt(stories.getInt("totalviews"));
			out.writeInt(stories.getInt("comments"));
			out.writeInt(stories.getInt("authorid"));
			out.writeUTF(nullToEmptyString(stories.getString("contentrating")));
			out.writeInt(stories.getInt("likes"));
			out.writeInt(stories.getInt("dislikes"));
			out.writeInt(stories.getInt("words"));
			out.writeUTF(nullToEmptyString(stories.getString("filename")));
			out.writeBoolean(dumpEpub);
			if(dumpEpub) {
				final File f = new File(storyDownloadDirectory, stories.getString("filename"));
				out.writeLong(f.length());
				final InputStream is = new FileInputStream(f);
				Util.copyStream(is, out);
				is.close();
			}
		}
		
		final ResultSet chapters = databaseConnection.createStatement().executeQuery("SELECT * FROM chapters");
		while(chapters.next()) {
			out.write(2);
			out.writeInt(chapters.getInt("id"));
			out.writeInt(chapters.getInt("updateid"));
			out.writeInt(chapters.getInt("chapterid"));
			out.writeInt(chapters.getInt("storyindex"));
			out.writeUTF(nullToEmptyString(chapters.getString("title")));
			out.writeInt(chapters.getInt("words"));
			out.writeInt(chapters.getInt("views"));
			out.writeLong(chapters.getDate("modifytime").getTime());
		}
		
		final ResultSet authors = databaseConnection.createStatement().executeQuery("SELECT * FROM authors");
		while(authors.next()) {
			out.write(3);
			out.writeInt(authors.getInt("id"));
			out.writeInt(authors.getInt("authorid"));
			out.writeUTF(nullToEmptyString(authors.getString("name")));
		}
		System.out.println("Dumped!");
	}
	
	private void loadFiles(final InputStream is) throws IOException, SQLException {
		final DataInputStream in = new DataInputStream(is);
		while(true) {
			final int next = in.read();
			if(next < 0) {
				break;
			} else if(next == 1) {
				final PreparedStatement prepared = databaseConnection.prepareStatement("INSERT INTO stories (storyid, description, shortdescription, title, modifytime, imagelocation, fullimagelocation, views, totalviews, comments, authorid, status, contentrating, likes, dislikes, words, filename) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
				int index = 1;
				// old DB ID, unused
				in.readInt();
				
				prepared.setInt(index++, in.readInt());
				prepared.setString(index++, in.readUTF());
				prepared.setString(index++, in.readUTF());
				prepared.setString(index++, in.readUTF());
				prepared.setDate(index++, new Date(in.readLong()));
				prepared.setString(index++, in.readUTF());
				prepared.setString(index++, in.readUTF());
				prepared.setInt(index++, in.readInt());
				prepared.setInt(index++, in.readInt());
				prepared.setInt(index++, in.readInt());
				prepared.setInt(index++, in.readInt());
				prepared.setString(index++, in.readUTF());
				prepared.setInt(index++, in.readInt());
				prepared.setInt(index++, in.readInt());
				prepared.setInt(index++, in.readInt());
				final String fname = in.readUTF();
				prepared.setString(index++, fname);
				if(in.readBoolean()) {
					long remainingBytes = in.readLong();
					final OutputStream os = new FileOutputStream(new File(storyDownloadDirectory, fname));
					final byte[] tmp = new byte[1024];
					while(remainingBytes > 0) {
						final int read = (int)Math.min(tmp.length, remainingBytes);
						remainingBytes -= read;
						in.read(tmp, 0, read);
						os.write(tmp, 0, read);
					}
					os.close();
				}
			} else if(next == 2) {
				final PreparedStatement cprepared = databaseConnection.prepareStatement("INSERT INTO chapters (updateid, chapterid, storyindex, title, words, views, modifytime) VALUES (?, ?, ?, ?, ?, ?, ?)");
				int index = 1;
				// old DB ID, unused
				in.readInt();
				
				cprepared.setInt(index++, in.readInt());
				cprepared.setInt(index++, in.readInt());
				cprepared.setInt(index++, in.readInt());
				cprepared.setString(index++, in.readUTF());
				cprepared.setInt(index++, in.readInt());
				cprepared.setInt(index++, in.readInt());
				cprepared.setDate(index++, new Date(in.readLong()));
				cprepared.execute();
			} else if(next == 3) {
				// old DB ID, unused
				in.readInt();
				final PreparedStatement aprepared = databaseConnection.prepareStatement("INSERT INTO authors (authorid, name) VALUES (?, ?)");
				aprepared.setInt(1, in.readInt());
				aprepared.setString(2, in.readUTF());
				aprepared.execute();
			}
		}
	}
	
	private String nullToEmptyString(String s) {
		return s == null ? "" : s;
	}
}
