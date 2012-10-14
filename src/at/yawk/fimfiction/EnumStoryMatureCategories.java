package at.yawk.fimfiction;

public enum EnumStoryMatureCategories {
	ALL(0), SEX(1), GORE(2);
	
	private final int	searchId;
	
	private EnumStoryMatureCategories(final int searchId) {
		this.searchId = searchId;
	}
	
	public int getSearchId() {
		return searchId;
	}
}
