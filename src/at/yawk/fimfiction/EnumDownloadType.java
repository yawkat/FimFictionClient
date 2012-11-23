package at.yawk.fimfiction;

public enum EnumDownloadType {
	EPUB("epub"),
	TXT("txt"),
	HTML("html");
	
	private final String fileType;
	private EnumDownloadType(final String fileType) {
		this.fileType = fileType;
	}
	
	public String getFileType() {
		return fileType;
	}
	
	public static EnumDownloadType parse(String s) {
		for(EnumDownloadType e : values())
			if(e.fileType.equals(s))
				return e;
		return null;
	}
}
