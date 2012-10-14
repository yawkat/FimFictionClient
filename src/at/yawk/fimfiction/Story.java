package at.yawk.fimfiction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

public final class Story {
	private final int				id;
	private String					description;
	private String					shortDescription;
	private String					title;
	private Chapter[]				chapters;
	private Date					modifyTime;
	private String					imageLocation;
	private String					fullImageLocation;
	private int						views;
	private int						totalViews;
	private int						comments;
	private Author					author;
	private EnumStoryStatus			status;
	private EnumStoryContentRating	contentRating;
	private int						likes;
	private int						dislikes;
	private int						words;
	
	public Story(final int id) {
		this.id = id;
	}
	
	public final int getId() {
		return id;
	}
	
	public final String getDescription() {
		return description;
	}
	
	public final void setDescription(final String description) {
		this.description = description;
	}
	
	public final void setTitle(final String title) {
		this.title = title;
	}
	
	public final String getTitle() {
		return title;
	}
	
	public Chapter[] getChapters() {
		return chapters;
	}
	
	public void setChapters(Chapter[] chapters) {
		this.chapters = chapters;
	}
	
	public String getShortDescription() {
		return shortDescription;
	}
	
	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}
	
	public Date getModifyTime() {
		return modifyTime;
	}
	
	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}
	
	public String getImageLocation() {
		return imageLocation;
	}
	
	public void setImageLocation(String imageLocation) {
		this.imageLocation = imageLocation;
	}
	
	public String getFullImageLocation() {
		return fullImageLocation;
	}
	
	public void setFullImageLocation(String fullImageLocation) {
		this.fullImageLocation = fullImageLocation;
	}
	
	public int getViews() {
		return views;
	}
	
	public void setViews(int views) {
		this.views = views;
	}
	
	public int getTotalViews() {
		return totalViews;
	}
	
	public void setTotalViews(int totalViews) {
		this.totalViews = totalViews;
	}
	
	public int getComments() {
		return comments;
	}
	
	public void setComments(int comments) {
		this.comments = comments;
	}
	
	public Author getAuthor() {
		return author;
	}
	
	public void setAuthor(Author author) {
		this.author = author;
	}
	
	public EnumStoryStatus getStatus() {
		return status;
	}
	
	public void setStatus(EnumStoryStatus status) {
		this.status = status;
	}
	
	public EnumStoryContentRating getContentRating() {
		return contentRating;
	}
	
	public void setContentRating(EnumStoryContentRating contentRating) {
		this.contentRating = contentRating;
	}
	
	public int getLikes() {
		return likes;
	}
	
	public void setLikes(int likes) {
		this.likes = likes;
	}
	
	public int getDislikes() {
		return dislikes;
	}
	
	public void setDislikes(int dislikes) {
		this.dislikes = dislikes;
	}
	
	public int getWords() {
		return words;
	}
	
	public void setWords(int words) {
		this.words = words;
	}
	
	public boolean equals(final Object o) {
		return o instanceof Story && o.hashCode() == hashCode();
	}
	
	public int hashCode() {
		final Collection<Integer> c = new ArrayList<>();
		nullSavePutHashCode(c, id);
		nullSavePutHashCode(c, author);
		nullSavePutHashCode(c, title);
		nullSavePutHashCode(c, contentRating);
		nullSavePutHashCode(c, description);
		nullSavePutHashCode(c, fullImageLocation);
		nullSavePutHashCode(c, imageLocation);
		nullSavePutHashCode(c, modifyTime);
		nullSavePutHashCode(c, shortDescription);
		if(chapters != null)
			c.add(Arrays.hashCode(chapters));
		nullSavePutHashCode(c, status);
		
		long hashCode = 0L;
		for(final Integer i : c) {
			hashCode += i;
			hashCode %= Integer.MAX_VALUE;
		}
		return (int)hashCode;
	}
	
	private void nullSavePutHashCode(final Collection<Integer> c, final Object o) {
		if(o != null)
			c.add(o.hashCode());
	}
}
