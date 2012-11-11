package at.yawk.fimfiction.examples.control.kindle;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;

import at.yawk.fimfiction.FimFictionConnectionAccount;
import at.yawk.fimfiction.examples.control.DownloadManager;
import at.yawk.kindle.Kindle;
import at.yawk.kindle.KindleFactory;

public class KindleManagerScreen extends JPanel {
	private static final long					serialVersionUID	= 1L;
	
	private final FimFictionConnectionAccount	connection;
	private final DownloadManager				dlManager;
	
	public KindleManagerScreen(FimFictionConnectionAccount ffc, DownloadManager dlManager) {
		this.connection = ffc;
		this.dlManager = dlManager;
		
		setLayout(new BorderLayout());
		
		initializeScreens();
	}
	
	private final void initializeScreens() {
		removeAll();
		
		final Kindle[] connectedKindles = KindleFactory.getAllRootConnectedKindles();
		if(connectedKindles.length == 0) {
			add(new JLabel("No Kindle detected."));
		} else {
			final boolean showKindleChooser = connectedKindles.length > 1;
			final List<Component> screens = new ArrayList<Component>(connectedKindles.length);
			
			for(final Kindle k : connectedKindles) {
				screens.add(new KindleDisplayScreen(connection, dlManager, k));
			}
			
			if(showKindleChooser) {
				final JTabbedPane tabs = new JTabbedPane();
				for(int i = 0; i < screens.size(); i++) {
					tabs.addTab(connectedKindles[i].getKindleRoot().toString(), screens.get(i));
				}
				add(tabs);
			} else {
				add(screens.get(0));
			}
		}
	}
}
