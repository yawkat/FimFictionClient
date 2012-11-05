package at.yawk.fimfiction.examples.control;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import at.yawk.fimfiction.EnumDownloadType;
import at.yawk.fimfiction.FimFictionConnectionAccount;
import at.yawk.fimfiction.SearchRequestBuilder;
import at.yawk.fimfiction.Util;

public class MainScreen extends JPanel {
	private static final long		serialVersionUID	= 1L;
	private final JMenuBar			menubar;
	private final JTabbedPane		tabs;
	private final DownloadManager	dlManager			= new DownloadManager();
	
	public MainScreen(final FimFictionConnectionAccount ffc, final Main main) {
		setLayout(new BorderLayout());
		{
			menubar = new JMenuBar();
			final JMenu account = new JMenu("FimFiction");
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
			final JMenu downloads = new JMenu("Downloads");
			{
				final JCheckBoxMenuItem[] checkboxes = new JCheckBoxMenuItem[EnumDownloadType.values().length];
				for(int i = 0; i < checkboxes.length; i++) {
					final int index = i;
					checkboxes[i] = new JCheckBoxMenuItem();
					checkboxes[i].setAction(new AbstractAction() {
						private static final long	serialVersionUID	= 1L;
						
						@Override
						public void actionPerformed(ActionEvent arg0) {
							dlManager.setStandardDownloadType(EnumDownloadType.values()[index]);
							for(int i = 0; i < checkboxes.length; i++) {
								checkboxes[i].setSelected(dlManager.getStandardDownloadType() == EnumDownloadType.values()[i]);
							}
						}
					});
					checkboxes[i].setSelected(dlManager.getStandardDownloadType() == EnumDownloadType.values()[i]);
					checkboxes[i].setText(EnumDownloadType.values()[i].toString());
					checkboxes[i].setVisible(true);
					downloads.add(checkboxes[i]);
				}
			}
			downloads.addSeparator();
			{
				final JMenuItem targetDir = new JMenuItem(new AbstractAction() {
					private static final long	serialVersionUID	= 1L;

					@Override
					public void actionPerformed(ActionEvent arg0) {
						final JFileChooser jf = new JFileChooser(dlManager.getDownloadDirectory());
						jf.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						jf.setAcceptAllFileFilterUsed(false);
						jf.setMultiSelectionEnabled(false);
						if(jf.showOpenDialog(MainScreen.this) == JFileChooser.APPROVE_OPTION) {
							dlManager.setDownloadDirectory(jf.getSelectedFile());
						}
					}
				});
				targetDir.setText("Download Directory");
				downloads.add(targetDir);
			}
			menubar.add(downloads);
			menubar.setVisible(true);
			add(menubar, BorderLayout.PAGE_START);
		}
		{
			tabs = new JTabbedPane();
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
			tabs.addTab("Search", new CustomSearch(ffc, dlManager));
			tabs.addTab("Favorites", new SearchDisplayTable(Util.FIMFICTION + "index.php?" + new SearchRequestBuilder().setMustBeFavorite(true).getRequest(), ffc, false, dlManager));
			tabs.addTab("Unread", new SearchDisplayTable(Util.FIMFICTION + "index.php?" + new SearchRequestBuilder().setMustBeFavorite(true).setMustBeUnread(true).getRequest(), ffc, false, dlManager));
			tabs.addTab("Read Later", new SearchDisplayTable(Util.FIMFICTION + "index.php?" + new SearchRequestBuilder().setMustBeReadLater(true).getRequest(), ffc, false, dlManager));
			tabs.addTab("Downloads", new DownloadList(dlManager));
			tabs.setVisible(true);
			add(tabs);
		}
		setVisible(true);
	}
}
