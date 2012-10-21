package at.yawk.fimfiction;

public enum EnumStoryStatus {
	COMPLETE, INCOMPLETE, ON_HIATUS, CANCELLED;
	
	public static EnumStoryStatus parse(final String s) {
		switch(s.toLowerCase()) {
		case "complete":
			return COMPLETE;
		case "incomplete":
			return INCOMPLETE;
		case "on hiatus":
			return ON_HIATUS;
		case "cancelled":
			return CANCELLED;
		default:
			return null;
		}
	}
}
