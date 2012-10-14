package at.yawk.fimfiction;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class FimFictionConnectionStandard implements IFimFictionConnection {

	@Override
	public URLConnection getConnection(URL url) throws IOException {
		final URLConnection urlc = url.openConnection();
		urlc.setRequestProperty("Cookie", "view_mature=true");
		return urlc;
	}
	
}
