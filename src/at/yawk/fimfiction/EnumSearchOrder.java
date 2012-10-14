package at.yawk.fimfiction;

public enum EnumSearchOrder {
	FIRST_POSTED_DATE("latest"),
	HOT("heat"),
	UPDATE_DATE("updated"),
	RATING("top"),
	VIEWS("views"),
	WORD_COUNT("words"),
	COMMENTS("comments");
	
	private final String searchValue;
	
	private EnumSearchOrder(final String searchValue) {
		this.searchValue = searchValue;
	}
	
	public String getSearchValue() {
		return searchValue;
	}
}
