package at.yawk.fimfiction;

import java.util.EnumMap;

public class SearchRequestBuilder {
	private String							searchTerm			= "";
	private EnumSearchOrder					searchOrder			= EnumSearchOrder.FIRST_POSTED_DATE;
	private EnumMap<EnumCategory, Boolean>	categories			= new EnumMap<>(EnumCategory.class);
	private EnumStoryContentRating			contentRating		= EnumStoryContentRating.ALL;
	private EnumStoryMatureCategories		matureCategories	= EnumStoryMatureCategories.ALL;
	private boolean							mustBeCompleted		= false;
	private Integer							minimumWords		= null;
	private Integer							maximumWords		= null;
	private EnumMap<EnumCharacter, Boolean>	characters			= new EnumMap<>(EnumCharacter.class);
	private boolean							mustBeUnread		= false;
	private boolean							mustBeFavorite		= false;
	
	public String getSearchTerm() {
		return searchTerm;
	}
	
	public SearchRequestBuilder setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
		return this;
	}
	
	public EnumSearchOrder getSearchOrder() {
		return searchOrder;
	}
	
	public SearchRequestBuilder setSearchOrder(EnumSearchOrder searchOrder) {
		this.searchOrder = searchOrder;
		return this;
	}
	
	public EnumMap<EnumCategory, Boolean> getCategories() {
		return categories;
	}
	
	public SearchRequestBuilder setCategories(EnumMap<EnumCategory, Boolean> categories) {
		this.categories = categories;
		return this;
	}
	
	public EnumStoryContentRating getContentRating() {
		return contentRating;
	}
	
	public SearchRequestBuilder setContentRating(EnumStoryContentRating contentRating) {
		this.contentRating = contentRating;
		return this;
	}
	
	public EnumStoryMatureCategories getMatureCategories() {
		return matureCategories;
	}
	
	public SearchRequestBuilder setMatureCategories(EnumStoryMatureCategories matureCategories) {
		this.matureCategories = matureCategories;
		return this;
	}
	
	public boolean isMustBeCompleted() {
		return mustBeCompleted;
	}
	
	public SearchRequestBuilder setMustBeCompleted(boolean mustBeCompleted) {
		this.mustBeCompleted = mustBeCompleted;
		return this;
	}
	
	public Integer getMinimumWords() {
		return minimumWords;
	}
	
	public SearchRequestBuilder setMinimumWords(Integer minimumWords) {
		this.minimumWords = minimumWords;
		return this;
	}
	
	public Integer getMaximumWords() {
		return maximumWords;
	}
	
	public SearchRequestBuilder setMaximumWords(Integer maximumWords) {
		this.maximumWords = maximumWords;
		return this;
	}
	
	public EnumMap<EnumCharacter, Boolean> getCharacters() {
		return characters;
	}
	
	public SearchRequestBuilder setCharacters(EnumMap<EnumCharacter, Boolean> characters) {
		this.characters = characters;
		return this;
	}
	
	public boolean isMustBeUnread() {
		return mustBeUnread;
	}
	
	public SearchRequestBuilder setMustBeUnread(boolean mustBeUnread) {
		this.mustBeUnread = mustBeUnread;
		return this;
	}
	
	public boolean isMustBeFavorite() {
		return mustBeFavorite;
	}
	
	public SearchRequestBuilder setMustBeFavorite(boolean mustBeFavorite) {
		this.mustBeFavorite = mustBeFavorite;
		return this;
	}
	
	public String getRequest() {
		final StringBuilder sb = new StringBuilder();
		
		sb.append("view=category");
		
		sb.append("&search=");
		sb.append(searchTerm);
		
		sb.append("&order=");
		sb.append(searchOrder.getSearchValue());
		
		for(final EnumCategory ec : EnumCategory.values()) {
			final Boolean b = categories.get(ec);
			sb.append('&');
			sb.append(ec.getSearchValue());
			sb.append('=');
			sb.append(b == null ? "" : b.booleanValue() ? "1" : "2");
		}
		
		sb.append("&content_rating=");
		sb.append(contentRating.getSearchId());
		
		sb.append("&mature_categories=");
		sb.append(matureCategories.getSearchId());
		
		if(mustBeCompleted)
			sb.append("&completed=1");
		
		if(mustBeFavorite)
			sb.append("&tracking");
		
		if(mustBeUnread)
			sb.append("&unread");
		
		sb.append("&minimum_words=");
		sb.append(minimumWords == null ? "" : minimumWords);
		
		sb.append("&maximum_words=");
		sb.append(maximumWords == null ? "" : maximumWords);

		for(final EnumCharacter ec : EnumCharacter.values()) {
			final Boolean b = characters.get(ec);
			if(b != null) {
				sb.append(b.booleanValue() ? "&characters[]=" : "&characters_execluded[]=");
				sb.append(ec.getId());
			}
		}
		
		return sb.toString();
	}
	
	public String toString() {
		return getRequest();
	}
	
	public boolean matches(final Story s) {
		boolean b = s.getTitle().contains(searchTerm);
		b &= contentRating.matches(s.getContentRating());
		b &= (!mustBeCompleted || s.getStatus() == EnumStoryStatus.COMPLETE);
		b &= (minimumWords == null || s.getWords() >= minimumWords);
		b &= (maximumWords == null || s.getWords() <= maximumWords);
		return b;
	}
}
