package at.yawk.fimfiction;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class FimFictionConnectionStandard implements IFimFictionConnection {
	private boolean	mature	= true;
	private final IWebProvider web;
	
	public FimFictionConnectionStandard(final IWebProvider web) {
		this.web = web;
	}
	
	public FimFictionConnectionStandard() {
		this(new StandardInternetProvider());
	}
	
	@Override
	public IURLConnection getConnection(URL url) throws IOException {
		final IURLConnection urlc = web.getConnection(url);
		if(mature)
			urlc.setHeader("Cookie", "view_mature=true");
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
