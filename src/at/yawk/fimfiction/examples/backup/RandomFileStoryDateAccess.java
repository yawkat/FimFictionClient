package at.yawk.fimfiction.examples.backup;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import at.yawk.fimfiction.Story;

import com.thoughtworks.xstream.XStream;

public class RandomFileStoryDateAccess implements IStoryDateAccess {
	private final int			STORY_FILE_ENTRY_BYTES			= Long.SIZE / Byte.SIZE;
	private final int			STORY_FILE_ENTRY_OFFSET_BYTES	= Integer.SIZE / Byte.SIZE;
	
	private RandomAccessFile	fileAccess;
	private Lock				fileLock						= new ReentrantLock();
	private Map<Integer, Story>	temporaryStoryStorage			= Collections.synchronizedMap(new HashMap<Integer, Story>(50));
	
	@Override
	public void setStoryForIndex(int index, Story story) {
		if(story.getModifyTime() == null) {
			temporaryStoryStorage.put(index, story);
		} else {
			temporaryStoryStorage.remove(index);
			setModifyDateFile(index, story.getModifyTime().getTime());
		}
	}
	
	@Override
	public Story getStoryForIndex(int index) {
		if(temporaryStoryStorage.containsKey(index)) {
			return temporaryStoryStorage.get(index);
		} else if(hasStoryFile(index)) {
			final Story s = new Story(index);
			s.setModifyTime(new Date(getModifyDateFile(index)));
			return s;
		} else {
			return null;
		}
	}
	
	@Override
	public boolean hasStory(int index) {
		return temporaryStoryStorage.containsKey(index) || hasStoryFile(index);
	}
	
	@Override
	public void save(File rootDir, XStream xs) throws IOException {
		for(final Entry<Integer, Story> e : temporaryStoryStorage.entrySet()) {
			setModifyDateFile(e.getKey(), -1);
		}
	}
	
	@Override
	public void load(File rootDir, XStream xs) throws IOException {
		fileLock.lock();
		try {
			fileAccess = new RandomAccessFile(new File(rootDir, "stories.dat"), "rw");
			if(fileAccess.length() == 0) {
				fileAccess.seek(0);
				fileAccess.writeInt(0);
			} else {
				final int length = getMaxStoryIdFile();
				for(int i = 0; i < length; i++) {
					if(getModifyDateFile(i) == -1L) {
						setStoryForIndex(i, new Story(i));
					}
				}
			}
		} finally {
			fileLock.unlock();
		}
	}
	
	@Override
	public Collection<Story> getAllStoriesInMemory() {
		return Collections.unmodifiableCollection(temporaryStoryStorage.values());
	}
	
	@Override
	public int getTotalStoryCount() {
		return temporaryStoryStorage.size() + getStoryCountFile();
	}
	
	private void setModifyDateFile(int index, long date) {
		fileLock.lock();
		try {
			if(!hasStoryFile(index)) {
				fileAccess.seek(0);
				fileAccess.writeInt(getStoryCountFile() + 1);
			}
			fileAccess.seek(STORY_FILE_ENTRY_OFFSET_BYTES + index * STORY_FILE_ENTRY_BYTES);
			fileAccess.writeLong(date + 1L);
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			fileLock.unlock();
		}
	}
	
	private long getModifyDateFile(int index) {
		if(hasStory(index)) {
			fileLock.lock();
			try {
				fileAccess.seek(STORY_FILE_ENTRY_OFFSET_BYTES + index * STORY_FILE_ENTRY_BYTES);
				return fileAccess.readLong() - 1L;
			} catch(IOException e) {
				e.printStackTrace();
			} finally {
				fileLock.unlock();
			}
		}
		return 0L;
	}
	
	private boolean hasStoryFile(int index) {
		fileLock.lock();
		try {
			if(fileAccess.length() >= STORY_FILE_ENTRY_OFFSET_BYTES + (index + 1) * STORY_FILE_ENTRY_BYTES) {
				fileAccess.seek(STORY_FILE_ENTRY_OFFSET_BYTES + index * STORY_FILE_ENTRY_BYTES);
				return fileAccess.readLong() != 0L;
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			fileLock.unlock();
		}
		return false;
	}
	
	private int getMaxStoryIdFile() {
		fileLock.lock();
		try {
			return (int)((fileAccess.length() - STORY_FILE_ENTRY_OFFSET_BYTES) / STORY_FILE_ENTRY_BYTES + 1);
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			fileLock.unlock();
		}
		return 0;
	}
	
	private int getStoryCountFile() {
		fileLock.lock();
		try {
			fileAccess.seek(0);
			return fileAccess.readInt();
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			fileLock.unlock();
		}
		return 0;
	}

	@Override
	public boolean isSlowAccess() {
		return true;
	}
}
