package at.yawk.fimfiction.examples.backup;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import com.thoughtworks.xstream.XStream;

import at.yawk.fimfiction.Story;

public interface IStoryDateAccess {
	public void setStoryForIndex(int index, Story story);
	public Story getStoryForIndex(int index);
	public boolean hasStory(int index);
	public void save(File rootDir, XStream xs) throws IOException;
	public void load(File rootDir, XStream xs) throws IOException;
	public Collection<Story> getAllStoriesInMemory();
	public int getTotalStoryCount();
	public boolean isSlowAccess();
}
