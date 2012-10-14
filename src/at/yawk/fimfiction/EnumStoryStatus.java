package at.yawk.fimfiction;

public enum EnumStoryStatus {
	COMPLETE, INCOMPLETE, ON_HIATUS;
	
	public static EnumStoryStatus parse(final String s) {
		switch(s.toLowerCase()) {
		case "complete":
			return COMPLETE;
		case "incomplete":
			return INCOMPLETE;
		case "on hiatus":
			return ON_HIATUS;
		default:
			return null;
		}
	}
}
