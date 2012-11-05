package at.yawk.fimfiction;

public enum EnumStoryMatureCategories {
	ALL(0, "All"), SEX(1, "Sex"), GORE(2, "Gore");
	
	private final int	searchId;
	private final String toString;
	
	private EnumStoryMatureCategories(final int searchId, final String toString) {
		this.searchId = searchId;
		this.toString = toString;
	}
	
	public int getSearchId() {
		return searchId;
	}
	
	public boolean matches(EnumStoryMatureCategories e) {
		return this == ALL || this == e;
	}
	
	@Override
	public String toString() {
		return toString;
	}
}
