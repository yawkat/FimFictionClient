package at.yawk.fimfiction;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public final class Stories {
	private Stories() {
		
	}
	
	public static void downloadStory(final Story s, final OutputStream output, final EnumDownloadType downloadType) throws MalformedURLException, IOException {
		String inputUrl;
		switch(downloadType) {
		case EPUB:
			inputUrl = Util.FIMFICTION + "download_epub.php?story=" + s.getId();
			break;
		case HTML:
			inputUrl = Util.FIMFICTION + "download_story.php?html&story=" + s.getId();
			break;
		case TXT:
			inputUrl = Util.FIMFICTION + "download_story.php?story=" + s.getId();
			break;
		default:
			throw new NullPointerException("Unknown download type: "+downloadType);
		}
		Util.copyStream(Util.getURLInputStream(new URL(inputUrl)), output);
	}
	
	public static void updateStory(final Story s) {
		//final Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		
	}
}
