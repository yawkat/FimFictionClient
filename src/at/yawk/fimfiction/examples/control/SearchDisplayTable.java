package at.yawk.fimfiction.examples.control;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.json.JSONException;

import at.yawk.fimfiction.IFimFictionConnection;
import at.yawk.fimfiction.Searches;
import at.yawk.fimfiction.Stories;
import at.yawk.fimfiction.Story;
import at.yawk.fimfiction.StoryID;

public class SearchDisplayTable extends JPanel implements ISelectionNotify {
	private static final long			serialVersionUID		= 1L;
	
	private final Runnable				updateRunnable;
	private final Set<Story>			stories					= Collections.synchronizedSet(new LinkedHashSet<Story>());
	private final Set<Story>			tableContainingStories	= Collections.synchronizedSet(new LinkedHashSet<Story>());
	private final JScrollPane			scrollpane;
	private final JTable				mainPanel;
	private final DefaultTableModel		model;
	
	private boolean						doneUpdating			= true;
	private boolean						shouldRestart			= true;
	private boolean						waiting					= false;
	private boolean						waitingScrolling		= false;
	private final boolean				partialLoading;
	private final DownloadManager		dlManager;
	private final IFimFictionConnection	connection;
	
	public SearchDisplayTable(final String request, final IFimFictionConnection connection, final boolean partialLoading, final DownloadManager manager, final JMenuItem... additionalItems) {
		this.dlManager = manager;
		this.connection = connection;
		this.partialLoading = partialLoading;
		mainPanel = new JTable(model = new DefaultTableModel() {
			private static final long	serialVersionUID	= 1L;
			
			@Override
			public boolean isCellEditable(int rowIndex, int mColIndex) {
				return mColIndex == 2;
			}
			
			@Override
			public Class<?> getColumnClass(int c) {
				if(c == 2) {
					return JButton.class;
				}
				return super.getColumnClass(c);
			}
		});
		mainPanel.setAutoCreateRowSorter(true);
		{
			final TableColumn tc = new TableColumn(0);
			tc.setHeaderValue("Title");
			mainPanel.addColumn(tc);
			model.addColumn(tc.getHeaderValue());
		}
		{
			final TableColumn tc = new TableColumn(1);
			tc.setHeaderValue("Author");
			mainPanel.addColumn(tc);
			model.addColumn(tc.getHeaderValue());
		}
		{
			final TableColumn tc = new TableColumn(2);
			tc.setHeaderValue("");
			mainPanel.addColumn(tc);
			model.addColumn(tc.getHeaderValue());
		}
		setLayout(new GridLayout(1, 1));
		mainPanel.setVisible(true);
		scrollpane = new JScrollPane(mainPanel);
		scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollpane.getVerticalScrollBar().setUnitIncrement(scrollpane.getVerticalScrollBar().getUnitIncrement() * 10);
		scrollpane.setVisible(true);
		add(scrollpane);
		updateData();
		final JPopupMenu refresh = new JPopupMenu();
		for(JMenuItem item : additionalItems)
			refresh.add(item);
		final JMenuItem item = new JMenuItem();
		item.setAction(new AbstractAction() {
			private static final long	serialVersionUID	= 1L;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				refresh();
			}
		});
		item.setText("Refresh");
		refresh.add(item);
		scrollpane.setComponentPopupMenu(refresh);
		mainPanel.setComponentPopupMenu(refresh);
		updateRunnable = new Runnable() {
			@Override
			public void run() {
				doneUpdating = false;
				item.setEnabled(doneUpdating);
				while(model.getRowCount() > 0)
					model.removeRow(0);
				final Iterator<Story> stories = Searches.parseFullSearchPartially(request, connection, 0);
				final Set<Story> allStories = new HashSet<>(SearchDisplayTable.this.stories.size() == 0 ? 16 : SearchDisplayTable.this.stories.size());
				while(stories.hasNext()) {
					while(waiting || waitingScrolling)
						try {
							TimeUnit.MILLISECONDS.sleep(100L);
						} catch(InterruptedException e1) {
							e1.printStackTrace();
						}
					final Story s = new StoryID(stories.next());
					if(!SearchDisplayTable.this.stories.contains(s)) {
						try {
							Stories.updateStory(s, connection);
						} catch(IOException | JSONException e) {
							e.printStackTrace();
						}
						SearchDisplayTable.this.stories.add(s);
						updateData();
					}
					allStories.add(s);
				}
				doneUpdating = true;
				item.setEnabled(doneUpdating);
			}
		};
		scrollpane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			
			@Override
			public void adjustmentValueChanged(AdjustmentEvent arg0) {
				updateUpdating();
			}
		});
		
		mainPanel.getColumnModel().getColumn(2).setMaxWidth(20);
		mainPanel.getColumnModel().getColumn(2).setCellRenderer(new TableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable arg0, Object arg1, boolean arg2, boolean arg3, int arg4, int arg5) {
				return (Component)arg1;
			}
		});
		mainPanel.getColumnModel().getColumn(2).setCellEditor(new ButtonCellEdit());
		
		setVisible(true);
	}
	
	private void updateData() {
		for(final Story entry : stories) {
			if(!tableContainingStories.contains(entry)) {
				final JButton dl = new JButton();
				dl.setMargin(new Insets(0, 0, 0, 0));
				dl.setAction(new AbstractAction() {
					private static final long	serialVersionUID	= 1L;
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						dlManager.download(entry, connection);
					}
				});
				dl.setText("DL");
				model.addRow(new Object[] { entry.getTitle(), entry.getAuthor().getName(), dl });
				tableContainingStories.add(entry);
			}
		}
		mainPanel.revalidate();
	}
	
	/** Yes. */
	private void updateUpdating() {
		waitingScrolling = partialLoading && scrollpane.getVerticalScrollBar().getValue() < mainPanel.getHeight() - scrollpane.getHeight() - 40;
	}
	
	public void startUpdating() {
		waiting = false;
		if(shouldRestart) {
			shouldRestart = false;
			new Thread(updateRunnable).start();
		}
	}
	
	public void stopUpdating() {
		waiting = true;
	}
	
	public void refresh() {
		if(doneUpdating) {
			stories.clear();
			tableContainingStories.clear();
			shouldRestart = true;
			startUpdating();
		}
	}
	
	@Override
	public void select() {
		startUpdating();
	}
	
	@Override
	public void deselect() {
		stopUpdating();
	}
	
	private final class ButtonCellEdit extends AbstractCellEditor implements TableCellEditor {
		private static final long	serialVersionUID	= 1L;
		private Object				currentValue;
		
		@Override
		public Object getCellEditorValue() {
			Object result = currentValue;
			currentValue = null;
			return result;
		}
		
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			currentValue = value;
			return (Component)value;
		}
	}
}
