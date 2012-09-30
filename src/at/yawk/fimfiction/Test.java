package at.yawk.fimfiction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

public class Test {
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException, FileNotFoundException, IOException {
		Stories.downloadStory(new Story(6635), new FileOutputStream(new File(Util.WORKINGDIR, "test.html")), EnumDownloadType.HTML);
	}
	
}
