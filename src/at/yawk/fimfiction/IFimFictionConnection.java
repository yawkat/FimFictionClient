package at.yawk.fimfiction;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public interface IFimFictionConnection {
	public URLConnection getConnection(URL url) throws IOException;
	public void setDisplayMature(boolean mature);
	public boolean getDisplayMature();
}
