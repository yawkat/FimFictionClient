package at.yawk.kindle;

public class FileNameUtils {
	public static String trimBookNameToKindle(final String s) {
		return s.substring(0, s.length() >= 32 ? 32 : s.length()).replaceAll("[^a-zA-Z ]", " ").trim();
	}
}
