package at.yawk.fimfiction.examples.backup;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.thoughtworks.xstream.XStream;

import at.yawk.fimfiction.Story;

public class StandardStoryDateAccess implements IStoryDateAccess {
	private final Map<Integer, Story>	allStories		= Collections.synchronizedMap(new HashMap<Integer, Story>(100));

	@Override
	public void setStoryForIndex(int index, Story story) {
		allStories.put(index, story);
	}

	@Override
	public Story getStoryForIndex(int index) {
		return allStories.get(index);
	}

	@Override
	public void save(final File rootDir, final XStream xs) throws IOException {
		final File s = new File(rootDir, "stories.xml");
		final DataFileEntry[] entries = new DataFileEntry[allStories.size()];
		int i = 0;
		for(Entry<Integer, Story> e : allStories.entrySet()) {
			entries[i++] = new DataFileEntry(e.getKey(), e.getValue().getModifyTime() == null ? 0L : e.getValue().getModifyTime().getTime() / 1000L);
		}
		final Writer fw = new FileWriter(s);
		xs.toXML(entries, fw);
		fw.flush();
		fw.close();
	}

	private static class DataFileEntry {
		@SuppressWarnings("unused")
		public DataFileEntry() {
			
		}
		
		public DataFileEntry(int id, long modified) {
			this.id = id;
			this.lastModified = modified;
		}
		
		private int		id;
		private long	lastModified;
	}
	
	public static Class<?> dataFileEntryClass() {
		return DataFileEntry.class;
	}

	@Override
	public void load(File rootDir, XStream xs) throws IOException {
		final File s = new File(rootDir, "stories.xml");
		if(s.exists()) {
			final DataFileEntry[] entries = (DataFileEntry[])xs.fromXML(s);
			for(DataFileEntry dfe : entries) {
				final Story t = new Story(dfe.id);
				t.setModifyTime(new Date(dfe.lastModified * 1000L));
				setStoryForIndex(dfe.id, t);
			}
		}
	}

	@Override
	public boolean hasStory(int index) {
		return allStories.containsKey(index);
	}

	@Override
	public Collection<Story> getAllStoriesInMemory() {
		return Collections.unmodifiableCollection(allStories.values());
	}

	@Override
	public int getTotalStoryCount() {
		return allStories.size();
	}

	@Override
	public boolean isSlowAccess() {
		return false;
	}
}
