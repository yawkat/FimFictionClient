package at.yawk.fimfiction;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class FimFictionConnectionAccount implements IFimFictionConnection {
	private Map<String, String>	cookies	= new HashMap<String, String>();
	private boolean isLoggedIn = false;
	
	@Override
	public URLConnection getConnection(URL url) throws IOException {
		final URLConnection urlc = url.openConnection();
		final StringBuilder sb = new StringBuilder("view_mature=true; ");
		for(final Entry<String, String> e : cookies.entrySet()) {
			sb.append(e.getKey());
			sb.append('=');
			sb.append(e.getValue());
			sb.append("; ");
		}
		urlc.setRequestProperty("Cookie", sb.toString());
		return urlc;
	}
	
	public boolean login(final String username, final String password) {
		try {
			final URLConnection urlc = new URL(Util.FIMFICTION + "ajax_login.php").openConnection();
			urlc.setDoInput(true);
			urlc.setDoOutput(true);
			urlc.setUseCaches(false);
			urlc.connect();
			urlc.getOutputStream().write(("username=" + username + "&password=" + password).getBytes());
			urlc.getOutputStream().flush();
			final char c = (char)(urlc.getInputStream().read());
			if(c == '0') {
				String headerName;
				for(int i = 1; (headerName = urlc.getHeaderFieldKey(i)) != null; i++) {
					if(headerName.equals("Set-Cookie")) {
						final String s = urlc.getHeaderField(i);
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
}
