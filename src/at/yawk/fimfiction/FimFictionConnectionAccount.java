package at.yawk.fimfiction;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class FimFictionConnectionAccount implements IFimFictionConnection {
	private Map<String, String>	cookies		= new HashMap<String, String>();
	private boolean				isLoggedIn	= false;
	private boolean				mature		= true;
	private final IWebProvider	web;
	
	public FimFictionConnectionAccount(final IWebProvider web) {
		this.web = web;
	}
	
	public FimFictionConnectionAccount() {
		this(new StandardInternetProvider());
	}
	
	@Override
	public IURLConnection getConnection(URL url) throws IOException {
		final IURLConnection urlc = web.getConnection(url);
		final StringBuilder sb = new StringBuilder(mature ? "view_mature=true; " : "");
		for(final Entry<String, String> e : cookies.entrySet()) {
			sb.append(e.getKey());
			sb.append('=');
			sb.append(e.getValue());
			sb.append("; ");
		}
		urlc.setHeader("Cookie", sb.toString());
		return urlc;
	}
	
	public boolean login(final String username, final String password) {
		try {
			final IURLConnection urlc = web.getConnection(new URL(Util.FIMFICTION + "ajax_login.php"));
			urlc.connect();
			urlc.getOutputStream().write(("username=" + username + "&password=" + password).getBytes());
			urlc.getOutputStream().flush();
			final char c = (char)(urlc.getInputStream().read());
			if(c == '0') {
				String headerName;
				for(int i = 1; (headerName = urlc.getHeader(i)[0]) != null; i++) {
					if(headerName.equals("Set-Cookie")) {
						final String s = urlc.getHeader(i)[1];
						cookies.put(s.substring(0, s.indexOf('=')), s.substring(s.indexOf('=') + 1, s.indexOf(';')));
					}
				}
				return isLoggedIn = true;
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		return isLoggedIn = false;
	}
	
	public boolean isLoggedIn() {
		return isLoggedIn;
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
