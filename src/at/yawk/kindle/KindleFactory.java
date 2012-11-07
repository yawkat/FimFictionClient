package at.yawk.kindle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Free Kindles for everyone! Yay!
 * 
 * @author Jonalu
 * 
 */
public class KindleFactory {
	/**
	 * Typically used on a document root / mounted kindle.
	 */
	public static Kindle getKindleForFolder(final File f) {
		// Yes, I know this is very basic, but it is good at detecting a kindle
		// from given USB devices.
		if(new File(f, "documents").exists() && new File(f, "system").exists() && new File(f, "DONT_HALT_ON_REPAIR").exists()) {
			return new Kindle(f);
		}
		return null;
	}
	
	public static Kindle[] getAllRootConnectedKindles() {
		final List<Kindle> kindles = new ArrayList<>();
		for(final File f : File.listRoots()) {
			final Kindle k = getKindleForFolder(f);
			if(k != null)
				kindles.add(k);
		}
		final Kindle[] akindles = new Kindle[kindles.size()];
		kindles.toArray(akindles);
		return akindles;
	}
}
