package at.yawk.fimfiction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class Chapter {
	private final int id;
	private final Story story;
	private final int storyIndex;
	private String title;
	private int words;
	private int views;
	private Date modifyTime;
	
	public Chapter(final int id, final Story story, final int storyIndex) {
		this.id = id;
		this.story = story;
		this.storyIndex = storyIndex;
	}
	
	public int getId() {
		return id;
	}
	
	public Story getStory() {
		return story;
	}

	public int getStoryIndex() {
		return storyIndex;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getWords() {
		return words;
	}

	public void setWords(int words) {
		this.words = words;
	}

	public int getViews() {
		return views;
	}

	public void setViews(int views) {
		this.views = views;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}
	
	public boolean equals(Object o) {
		if(o instanceof Chapter) {
			final Chapter c = (Chapter)o;
			return id == c.id && storyIndex == c.storyIndex && nullSaveEquals(title, c.title) && nullSaveEquals(words, c.words) && nullSaveEquals(views, c.views) && nullSaveEquals(modifyTime, c.modifyTime);
		} else {
			return false;
		}
	}
	
	public int hashCode() {
		final Collection<Integer> c = new ArrayList<>();
		nullSavePutHashCode(c, id);
		nullSavePutHashCode(c, storyIndex);
		nullSavePutHashCode(c, title);
		nullSavePutHashCode(c, words);
		nullSavePutHashCode(c, views);
		nullSavePutHashCode(c, modifyTime);
		
		long hashCode = 0L;
		for(final Integer i : c) {
			hashCode += i;
			hashCode %= Integer.MAX_VALUE;
		}
		return (int)hashCode;
	}
	
	private boolean nullSaveEquals(Object o, Object p) {
		return (o == null && p == null) || (o != null && o.equals(p));
	}
	
	private void nullSavePutHashCode(final Collection<Integer> c, final Object o) {
		if(o != null)
			c.add(o.hashCode());
	}
}
