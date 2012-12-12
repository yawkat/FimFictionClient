package at.yawk.fimfiction.examples.control;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import at.yawk.fimfiction.Story;

public class DownloadList extends JPanel {
	private static final long					serialVersionUID	= 1L;
	private final JScrollPane					scrollpane;
	private final JTable						mainPanel;
	private final DefaultTableModel				model;
	private final Map<ProgressDisplay, Story>	rows				= new HashMap<ProgressDisplay, Story>();
	private final AtomicBoolean					repainting			= new AtomicBoolean(false);
	
	public DownloadList(final DownloadManager manager, final JMenuItem... additionalItems) {
		mainPanel = new JTable(model = new DefaultTableModel() {
			private static final long	serialVersionUID	= 1L;
			
			public boolean isCellEditable(int rowIndex, int mColIndex) {
				return false;
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
			tc.setHeaderValue("Status");
			mainPanel.addColumn(tc);
			model.addColumn(tc.getHeaderValue());
		}
		setLayout(new GridLayout(1, 1));
		scrollpane = new JScrollPane(mainPanel);
		scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollpane.getVerticalScrollBar().setUnitIncrement(scrollpane.getVerticalScrollBar().getUnitIncrement() * 10);
		scrollpane.setVisible(true);
		add(scrollpane);
		
		final JPopupMenu refresh = new JPopupMenu();
		for(JMenuItem item : additionalItems)
			refresh.add(item);
		final JMenuItem item = new JMenuItem();
		item.setAction(new AbstractAction() {
			private static final long	serialVersionUID	= 1L;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				clear();
			}
		});
		item.setText("Clear finished");
		refresh.add(item);
		scrollpane.setComponentPopupMenu(refresh);
		mainPanel.setComponentPopupMenu(refresh);
		
		mainPanel.getColumnModel().getColumn(1).setCellRenderer(new TableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				return (Component)value;
			}
		});
		
		manager.addListener(new DownloadManager.IDownloadListener() {
			@Override
			public IDownloadUpdate getDownloadUpdate(final Story story) {
				final ProgressDisplay pd = new ProgressDisplay();
				rows.put(pd, story);
				model.addRow(new Object[] { new Object() {
					@Override
					public String toString() {
						return story.getTitle() == null ? "Loading..." : story.toString();
					}
				}, pd });
				return pd;
			}
		});
	}
	
	private void clear() {
		for(int i = 0; i < model.getRowCount(); i++) {
			if(!rows.containsKey(model.getValueAt(i, 1)))
				model.removeRow(i--);
		}
	}
	
	private void markDirty() {
		if(!repainting.get()) {
			repainting.set(true);
			new Thread(new Runnable() {
				@Override
				public void run() {
					repaint();
					repainting.set(false);
				}
			}).start();
		}
	}
	
	private class ProgressDisplay extends JPanel implements IDownloadUpdate, Comparable<ProgressDisplay> {
		private static final long	serialVersionUID	= 1L;
		private final JProgressBar	progress;
		private float				progressFloat		= 0F;
		
		public ProgressDisplay() {
			progress = new JProgressBar();
			progress.setMaximum(1000);
			setLayout(new GridLayout(1, 1));
			add(progress);
			setBackground(new Color(0, 0, 0, 0));
		}
		
		@Override
		public void setProgress(float progress) {
			if(progress >= 1) {
				removeAll();
				final JLabel jl = new JLabel("Done");
				jl.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
				add(jl);
				rows.remove(this);
			} else {
				this.progress.setValue((int)(progress * 1000));
			}
			progressFloat = progress;
			markDirty();
		}
		
		@Override
		public int compareTo(ProgressDisplay o) {
			return Float.compare(this.progressFloat, o.progressFloat);
		}
	}
}
