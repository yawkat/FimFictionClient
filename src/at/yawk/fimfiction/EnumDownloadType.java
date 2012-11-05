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
}
