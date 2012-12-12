package at.yawk.fimfiction;

public enum EnumStoryStatus {
	COMPLETE, INCOMPLETE, ON_HIATUS, CANCELLED;
	
	public static EnumStoryStatus parse(final String s) {
		final int t = s.toLowerCase().hashCode();
		if(t == "complete".hashCode())
			return COMPLETE;
		if(t == "incomplete".hashCode())
			return INCOMPLETE;
		if(t == "on hiatus".hashCode())
			return ON_HIATUS;
		if(t == "cancelled".hashCode())
			return CANCELLED;
		return null;
	}
}
