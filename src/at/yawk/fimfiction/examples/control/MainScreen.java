package at.yawk.fimfiction.examples.control;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import at.yawk.fimfiction.FimFictionConnectionAccount;
import at.yawk.fimfiction.SearchRequestBuilder;
import at.yawk.fimfiction.Util;

public class MainScreen extends JPanel {
	private static final long	serialVersionUID	= 1L;
	private final JMenuBar		menubar;
	private final JTabbedPane	tabs;
	
	public MainScreen(final FimFictionConnectionAccount ffc, final Main main) {
		setLayout(new BorderLayout());
		{
			menubar = new JMenuBar();
			final JMenu account = new JMenu("Settings");
			{
				final JMenuItem logout = new JMenuItem(new AbstractAction() {
					private static final long	serialVersionUID	= 1L;
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						main.displayLoginScreen();
					}
				});
				logout.setText("Logout");
				logout.setVisible(true);
				account.add(logout);
			}
			{
				final JCheckBoxMenuItem mature = new JCheckBoxMenuItem();
				mature.setAction(new AbstractAction() {
					private static final long	serialVersionUID	= 1L;
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						ffc.setDisplayMature(!ffc.getDisplayMature());
						mature.setSelected(ffc.getDisplayMature());
					}
				});
				mature.setSelected(ffc.getDisplayMature());
				mature.setText("Mature");
				mature.setVisible(true);
				account.add(mature);
			}
			menubar.add(account);
			menubar.setVisible(true);
			add(menubar, BorderLayout.PAGE_START);
		}
		{
			tabs = new JTabbedPane();
			final SearchDisplayTable[] storyTabs = new SearchDisplayTable[] { new SearchDisplayTable(Util.FIMFICTION + "index.php?" + new SearchRequestBuilder().setMustBeFavorite(true).getRequest(), ffc, false), new SearchDisplayTable(Util.FIMFICTION + "index.php?" + new SearchRequestBuilder().setMustBeFavorite(true).setMustBeUnread(true).getRequest(), ffc, false), new SearchDisplayTable(Util.FIMFICTION + "index.php?" + new SearchRequestBuilder().setMustBeReadLater(true).getRequest(), ffc, false) };
			tabs.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent arg0) {
					for(final Component sdt : tabs.getComponents()) {
						if(sdt instanceof ISelectionNotify) {
							if(tabs.getSelectedComponent() == sdt)
								((ISelectionNotify)sdt).select();
							else
								((ISelectionNotify)sdt).deselect();
						}
					}
				}
			});
			tabs.addTab("Search", new CustomSearch(ffc));
			tabs.addTab("Favorites", storyTabs[0]);
			tabs.addTab("Unread", storyTabs[1]);
			tabs.addTab("Read Later", storyTabs[2]);
			tabs.setVisible(true);
			add(tabs);
		}
		setVisible(true);
	}
}
