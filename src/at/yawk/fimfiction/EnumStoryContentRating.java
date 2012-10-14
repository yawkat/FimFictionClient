package at.yawk.fimfiction;

public enum EnumStoryContentRating {
	ALL(-1), EVERYONE(0), TEEN(1), MATURE(2);
	
	private final int searchId;
	
	private EnumStoryContentRating(final int searchId) {
		this.searchId = searchId;
	}
	
	public int getSearchId() {
		return searchId;
	}
	
	public static EnumStoryContentRating parse(final String s) {
		switch(s.toLowerCase()) {
		case "all":
			return ALL;
		case "everyone":
			return EVERYONE;
		case "teen":
			return TEEN;
		case "mature":
			return MATURE;
		default:
			return null;
		}
	}
}
