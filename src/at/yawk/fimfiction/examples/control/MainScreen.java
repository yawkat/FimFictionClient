package at.yawk.fimfiction.examples.control;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
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
			final JMenu account = new JMenu("Account");
			final JMenuItem logout = new JMenuItem(new AbstractAction() {
				private static final long	serialVersionUID	= 1L;
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					main.displayLoginScreen();
				}
			});
			logout.setText("logout");
			logout.setVisible(true);
			account.add(logout);
			menubar.add(account);
			menubar.setVisible(true);
			add(menubar, BorderLayout.PAGE_START);
		}
		{
			tabs = new JTabbedPane();
			final SearchDisplayTable[] storyTabs = new SearchDisplayTable[] { new SearchDisplayTable(Util.FIMFICTION + "index.php?" + new SearchRequestBuilder().setMustBeReadLater(true).getRequest(), ffc) };
			tabs.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent arg0) {
					for(final SearchDisplayTable sdt : storyTabs)
						if(tabs.getSelectedComponent() == sdt)
							sdt.startUpdating();
						else
							sdt.stopUpdating();
				}
			});
			tabs.addTab("Favorites", storyTabs[0]);
			tabs.setVisible(true);
			add(tabs);
		}
		setVisible(true);
	}
}
