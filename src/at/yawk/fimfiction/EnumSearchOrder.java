package at.yawk.fimfiction;

public enum EnumSearchOrder {
	FIRST_POSTED_DATE("latest", "First posted Date"),
	HOT("heat", "Hot"),
	UPDATE_DATE("updated", "Update Date"),
	RATING("top", "Rating"),
	VIEWS("views", "Views"),
	WORD_COUNT("words", "Word count"),
	COMMENTS("comments", "Comments");
	
	private final String searchValue;
	private final String toString;
	
	private EnumSearchOrder(final String searchValue, final String toString) {
		this.searchValue = searchValue;
		this.toString = toString;
	}
	
	public String getSearchValue() {
		return searchValue;
	}
	
	public String toString() {
		return toString;
	}
}
