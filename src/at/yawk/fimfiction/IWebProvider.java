package at.yawk.fimfiction;

import java.io.IOException;
import java.net.URL;

public interface IWebProvider {
	public IURLConnection getConnection(final URL url) throws IOException;
}
