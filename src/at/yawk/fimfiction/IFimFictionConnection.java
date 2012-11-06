package at.yawk.fimfiction;

import java.io.IOException;
import java.net.URL;

public interface IFimFictionConnection {
	public IURLConnection getConnection(URL url) throws IOException;
	public void setDisplayMature(boolean mature);
	public boolean getDisplayMature();
}
