package at.yawk.fimfiction;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class FimFictionConnectionStandard implements IFimFictionConnection {
	private boolean	mature	= true;
	
	@Override
	public URLConnection getConnection(URL url) throws IOException {
		final URLConnection urlc = url.openConnection();
		if(mature)
			urlc.setRequestProperty("Cookie", "view_mature=true");
		return urlc;
	}
	
	@Override
	public void setDisplayMature(boolean mature) {
		this.mature = mature;
	}
	
	@Override
	public boolean getDisplayMature() {
		return mature;
	}
}
