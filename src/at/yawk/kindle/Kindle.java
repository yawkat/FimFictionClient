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
	private final File					bookRoot;
	private final File					kindleRoot;
	
	private final Map<String, Long>		specialModifyDate	= new HashMap<>();
	private final Map<String, String>	realName			= new HashMap<>();
	
	Kindle(final File kindleRootDir) {
		bookRoot = new File(kindleRoot = kindleRootDir, "documents");
		if(getSpecialModifyDateFile().exists()) {
			try {
				final DataInputStream dis = new DataInputStream(new FileInputStream(getSpecialModifyDateFile()));
				while(dis.available() > 0) {
					final String s = dis.readUTF();
					realName.put(s, dis.readUTF());
					specialModifyDate.put(s, dis.readLong());
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
		if(!specialModifyDate.isEmpty() || getSpecialModifyDateFile().exists()) {
			final DataOutputStream dos = new DataOutputStream(new FileOutputStream(getSpecialModifyDateFile()));
			for(final Entry<String, Long> e : specialModifyDate.entrySet()) {
				dos.writeUTF(e.getKey());
				dos.writeUTF(realName.get(e.getKey()));
				dos.writeLong(e.getValue());
			}
			dos.close();
		}
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
	
	public void setSpecialModifyDate(final String bookName, final String realBookName, final long modifyDate) {
		specialModifyDate.put(bookName, modifyDate);
		realName.put(bookName, realBookName);
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
	
	public Long getSpecialBookUpdate(final String bookName) {
		return specialModifyDate.get(bookName);
	}
	
	public long getBookUpdateDateTotal(final String bookName) {
		final Long l = getSpecialBookUpdate(bookName);
		return l == null ? getBookUpdateDateMilliseconds(bookName) : l;
	}
	
	private final File getSpecialModifyDateFile() {
		return new File(kindleRoot, "at.yawk.kindle.specialModifyDate.dat");
	}
	
	public String getRealBookName(final String bookName) {
		return realName.get(bookName);
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		saveSpecialDates();
	}
	
	public File getKindleRoot() {
		return kindleRoot;
	}
	
	public String[] listBooks() {
		final String[] list = bookRoot.list(new FilenameFilter() {
			@Override
			public boolean accept(File arg0, String arg1) {
				return arg1.matches("[a-zA-Z ]+-.+\\.azw");
			}
		});
		for(int i = 0; i < list.length; i++) {
			list[i] = list[i].substring(0, list[i].indexOf('-'));
		}
		return list;
	}
}
