package at.yawk.fimfiction.examples.control;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import at.yawk.fimfiction.IFimFictionConnection;
import at.yawk.fimfiction.SearchRequestBuilder;
import at.yawk.fimfiction.Util;

public class CustomSearch extends JPanel implements ISelectionNotify {
	private static final long		serialVersionUID	= 1L;
	private ISelectionNotify		childNotify			= null;
	private final DownloadManager	dlManager;
	private final EpubServer		server;
	
	public CustomSearch(final IFimFictionConnection connection, final DownloadManager dlManager, final EpubServer server) {
		setLayout(new GridLayout(1, 1));
		displaySettings(connection);
		this.dlManager = dlManager;
		this.server = server;
	}
	
	@Override
	public void select() {
		if(childNotify != null)
			childNotify.select();
	}
	
	@Override
	public void deselect() {
		if(childNotify != null)
			childNotify.deselect();
	}
	
	private void displaySettings(final IFimFictionConnection connection) {
		childNotify = null;
		removeAll();
		final SearchRequestBuilder builder = new SearchRequestBuilder();
		add(new SearchBuilderDisplay(builder, new Runnable() {
			@Override
			public void run() {
				removeAll();
				final JMenuItem mi = new JMenuItem();
				mi.setAction(new AbstractAction() {
					private static final long	serialVersionUID	= 1L;
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						displaySettings(connection);
					}
				});
				mi.setText("New Search");
				add((Component)(childNotify = new SearchDisplayTable(Util.FIMFICTION + "index.php?" + builder.getRequest(), connection, true, dlManager, server, mi)));
				select();
			}
		}));
	}
}
