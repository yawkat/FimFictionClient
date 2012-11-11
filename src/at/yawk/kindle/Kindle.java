package at.yawk.kindle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Kindle {
	private final File				bookRoot;
	private final File				kindleRoot;
	
	private final Map<String, Long>	specialModifyDate	= new HashMap<String, Long>();
	
	Kindle(final File kindleRootDir) {
		bookRoot = new File(kindleRoot = kindleRootDir, "documents");
		if(getSpecialModifyDateFile().exists()) {
			try {
				final DataInputStream dis = new DataInputStream(new FileInputStream(getSpecialModifyDateFile()));
				while(dis.available() > 0) {
					specialModifyDate.put(dis.readUTF(), dis.readLong());
				}
				dis.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					saveSpecialDates();
				} catch(IOException e) {}
			}
		}));
	}
	
	public void saveSpecialDates() throws IOException {
		final DataOutputStream dos = new DataOutputStream(new FileOutputStream(getSpecialModifyDateFile()));
		for(final Entry<String, Long> e : specialModifyDate.entrySet()) {
			dos.writeUTF(e.getKey());
			dos.writeLong(e.getValue());
		}
		dos.close();
	}
	
	public boolean checkOnKindle(final String bookName) {
		return bookRoot.list(new FilenameFilter() {
			@Override
			public boolean accept(File arg0, String arg1) {
				return arg1.matches(bookName + "-.+\\.azw");
			}
		}).length > 0;
	}
	
	public OutputStream getBookOutputStream(final String fullFileName) {
		try {
			return new FileOutputStream(new File(bookRoot, fullFileName));
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void setSpecialModifyDate(final String bookName, final long modifyDate) {
		specialModifyDate.put(bookName, modifyDate);
	}
	
	public void deleteFromKindle(final String bookName) {
		for(final File f : bookRoot.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File arg0, String arg1) {
				return arg1.matches(bookName + "-.+\\.(azw|mbp)");
			}
		}))
			f.delete();
	}
	
	public long getBookUpdateDateMilliseconds(final String bookName) {
		return bookRoot.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File arg0, String arg1) {
				return arg1.matches(bookName + "-.+\\.azw");
			}
		})[0].lastModified();
	}
	
	private final File getSpecialModifyDateFile() {
		return new File(kindleRoot, "at.yawk.kindle.specialModifyDate.dat");
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		saveSpecialDates();
	}
	
	public File getKindleRoot() {
		return kindleRoot;
	}
}
