package at.yawk.fimfiction;

public enum EnumStoryContentRating {
	ALL(-1, "All"), EVERYONE(0, "Everyone"), TEEN(1, "Teen"), MATURE(2, "Mature");
	
	private final int searchId;
	private final String toString;
	
	private EnumStoryContentRating(final int searchId, final String toString) {
		this.searchId = searchId;
		this.toString = toString;
	}
	
	public int getSearchId() {
		return searchId;
	}
	
	public static EnumStoryContentRating parse(final String s) {
		final int t = s.toLowerCase().trim().hashCode();
		if(t == "all".hashCode())
			return ALL;
		if(t == "everyone".hashCode())
			return EVERYONE;
		if(t == "teen".hashCode())
			return TEEN;
		if(t == "mature".hashCode())
			return MATURE;
		return null;
	}
	
	public boolean matches(EnumStoryContentRating e) {
		return this == ALL || this == e;
	}
	
	@Override
	public String toString() {
		return toString;
	}
} 
