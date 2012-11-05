package at.yawk.fimfiction;

import java.io.IOException;
import java.net.URL;

public class StandardInternetProvider implements IWebProvider {
	@Override
	public IURLConnection getConnection(URL url) throws IOException {
		return new StandardUrlConnection(url.openConnection());
	}
}
