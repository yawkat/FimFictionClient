package at.yawk.fimfiction.examples.control.kindle;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import at.yawk.fimfiction.FimFictionConnectionAccount;
import at.yawk.fimfiction.examples.control.DownloadManager;
import at.yawk.kindle.Kindle;

class KindleDisplayScreen extends JPanel {
	private static final long					serialVersionUID	= 1L;
	
	private final FimFictionConnectionAccount	connection;
	private final DownloadManager				dlManager;
	private final Kindle						kindle;
	
	private final JTable						downloadedEbooksTable;
	private final DefaultTableModel				downloadedEbooksModel;
	
	private final JTable						onKindleTable;
	private final DefaultTableModel				onKindleModel;
	
	public KindleDisplayScreen(FimFictionConnectionAccount ffc, DownloadManager dlManager, Kindle kindle) {
		this.connection = ffc;
		this.dlManager = dlManager;
		this.kindle = kindle;
		
		setLayout(new BorderLayout());
		
		{
			downloadedEbooksTable = new JTable();
			downloadedEbooksTable.setModel(downloadedEbooksModel = new DefaultTableModel() {
				private static final long	serialVersionUID	= 1L;
				
				@Override
				public boolean isCellEditable(int rowIndex, int mColIndex) {
					return mColIndex >= 2;
				}
			});
			
			{
				final TableColumn tc = new TableColumn(0);
				tc.setHeaderValue("Downloads");
				downloadedEbooksTable.addColumn(tc);
				downloadedEbooksModel.addColumn(tc.getHeaderValue());
			}

			final JScrollPane scroll = new JScrollPane(downloadedEbooksTable);
			scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			add(scroll, BorderLayout.LINE_START);
		}
		{
			onKindleTable = new JTable();
			onKindleTable.setModel(onKindleModel = new DefaultTableModel() {
				private static final long	serialVersionUID	= 1L;
				
				@Override
				public boolean isCellEditable(int rowIndex, int mColIndex) {
					return mColIndex >= 2;
				}
			});
			
			{
				final TableColumn tc = new TableColumn(0);
				tc.setHeaderValue("On Kindle");
				onKindleTable.addColumn(tc);
				onKindleModel.addColumn(tc.getHeaderValue());
			}

			final JScrollPane scroll = new JScrollPane(onKindleTable);
			scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			add(scroll, BorderLayout.LINE_END);
		}
		{
			onKindleModel.addRow(new String[] {"TEST"});
		}
		{
			final JPanel movePanel = new JPanel();
			movePanel.add(new JButton());
			add(movePanel, BorderLayout.CENTER);
		}
	}
}
