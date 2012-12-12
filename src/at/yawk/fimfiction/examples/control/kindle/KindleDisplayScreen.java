package at.yawk.fimfiction.examples.control.kindle;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.json.JSONException;

import at.yawk.fimfiction.FimFictionConnectionAccount;
import at.yawk.fimfiction.SearchRequestBuilder;
import at.yawk.fimfiction.Searches;
import at.yawk.fimfiction.Stories;
import at.yawk.fimfiction.Story;
import at.yawk.fimfiction.Util;
import at.yawk.fimfiction.examples.control.DownloadManager;
import at.yawk.fimfiction.examples.control.IDownloadUpdate;
import at.yawk.kindle.FileNameUtils;
import at.yawk.kindle.Kindle;

class KindleDisplayScreen extends JPanel {
	private static final long					serialVersionUID	= 1L;
	private final static ImageIcon				arrow;
	static {
		ImageIcon bi = null;
		try {
			bi = new ImageIcon(ImageIO.read(KindleDisplayScreen.class.getResource("arrow.png")));
		} catch(IOException e) {
			e.printStackTrace();
		}
		arrow = bi;
	}
	
	private final FimFictionConnectionAccount	connection;
	private final DownloadManager				dlManager;
	private final Kindle						kindle;
	
	private final JTable						downloadedEbooksTable;
	private final DefaultTableModel				downloadedEbooksModel;
	
	private final JTable						onKindleTable;
	private final DefaultTableModel				onKindleModel;
	
	private final JProgressBar					progress;
	
	private final Executor						threads				= Executors.newFixedThreadPool(10);
	
	public KindleDisplayScreen(FimFictionConnectionAccount ffc, final DownloadManager dlManager, final Kindle kindle) {
		this.connection = ffc;
		this.dlManager = dlManager;
		this.kindle = kindle;
		
		setLayout(new LayoutManager() {
			private Component	left, right, center, bottom;
			
			@Override
			public void removeLayoutComponent(Component arg0) {
				
			}
			
			@Override
			public Dimension preferredLayoutSize(Container arg0) {
				return getContainerBounds(arg0).getSize();
			}
			
			@Override
			public Dimension minimumLayoutSize(Container arg0) {
				return getContainerBounds(arg0).getSize();
			}
			
			@Override
			public void layoutContainer(Container arg0) {
				for(final Component c : arg0.getComponents())
					c.setBounds(getContainerBounds(c));
			}
			
			private Rectangle getContainerBounds(Component c) {
				final int width;
				final int x;
				if(c == left || c == right) {
					width = getWidth() / 2 - 15;
				} else if(c == center) {
					width = 30;
				} else {
					width = getWidth();
				}
				if(c == left) {
					x = 0;
				} else if(c == center) {
					x = getWidth() / 2 - 15;
				} else if(c == right) {
					x = getWidth() / 2 + 15;
				} else {
					x = 0;
				}
				return new Rectangle(x, c == bottom ? getHeight() - 20 : 0, width, c == bottom ? 20 : getHeight() - 20);
			}
			
			@Override
			public void addLayoutComponent(String arg0, Component arg1) {
				if(arg0.equalsIgnoreCase("left"))
					left = arg1;
				else if(arg0.equalsIgnoreCase("right"))
					right = arg1;
				else if(arg0.equalsIgnoreCase("center"))
					center = arg1;
				else if(arg0.equalsIgnoreCase("bottom"))
					bottom = arg1;
			}
		});
		
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
			add(scroll, "left");
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
			{
				final TableColumn tc = new TableColumn(1);
				tc.setHeaderValue("");
				onKindleTable.addColumn(tc);
				onKindleModel.addColumn(tc.getHeaderValue());
			}
			
			final JScrollPane scroll = new JScrollPane(onKindleTable);
			scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			add(scroll, "right");
			
			onKindleTable.getColumnModel().getColumn(1).setMinWidth(12);
			onKindleTable.getColumnModel().getColumn(1).setMaxWidth(12);
			onKindleTable.getColumnModel().getColumn(1).setCellRenderer(new TableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable arg0, Object arg1, boolean arg2, boolean arg3, int arg4, int arg5) {
					return arg1 instanceof Icon ? new JLabel((Icon)arg1) : new JLabel((String)arg1);
				}
			});
		}
		{
			progress = new JProgressBar();
			progress.setMaximum(4);
			add(progress, "bottom");
		}
		{
			final JPanel movePanel = new JPanel();
			movePanel.setLayout(new GridLayout());
			final JButton add = new JButton(new AbstractAction() {
				private static final long	serialVersionUID	= 1L;
				
				@Override
				public void actionPerformed(ActionEvent e) {
					for(final int i : downloadedEbooksTable.getSelectedRows()) {
						final String fname = (String)downloadedEbooksTable.getValueAt(i, 0);
						final File f = new File(dlManager.getDownloadDirectory(), fname);
						threads.execute(new Runnable() {
							@Override
							public void run() {
								final String targetTitle = FileNameUtils.trimBookNameToKindle(fname.substring(0, fname.length() - 5));
								try {
									final File mobi = File.createTempFile(targetTitle, ".mobi");
									progress.setValue(1);
									Runtime.getRuntime().exec("ebook-convert \"" + f.getAbsolutePath() + "\" \"" + f.getAbsolutePath() + "\"").waitFor();
									progress.setValue(2);
									final OutputStream os = kindle.getBookOutputStream(fname + ".mobi");
									final InputStream is = new FileInputStream(mobi);
									Util.copyStream(is, os);
									is.close();
									os.close();
									mobi.delete();
									progress.setValue(3);
									final Story s = dlManager.getStoryForFile(f);
									if(s != null) {
										if(s.getTitle() == null)
											try {
												Stories.updateStory(s, connection);
											} catch(Exception e) {
												e.printStackTrace();
											}
										kindle.setSpecialModifyDate(targetTitle, s.getTitle(), s.getModifyTime().getTime());
										kindle.saveSpecialDates();
									}
									progress.setValue(4);
								} catch(IOException e) {
									e.printStackTrace();
								} catch(InterruptedException e) {
									e.printStackTrace();
								}
							}
						});
					}
				}
			});
			add.setText(">");
			movePanel.add(add);
			add(movePanel, "center");
		}
		
		updateDownloads();
		updateKindle();
		dlManager.addListener(new DownloadManager.IDownloadListener() {
			@Override
			public IDownloadUpdate getDownloadUpdate(Story story) {
				return new IDownloadUpdate() {
					@Override
					public void setProgress(float progress) {
						if(progress == 1)
							updateDownloads();
					}
				};
			}
		});
	}
	
	private void updateKindle() {
		while(onKindleModel.getRowCount() > 0)
			onKindleModel.removeRow(0);
		final List<String[]> books = new ArrayList<String[]>();
		for(final String s : kindle.listBooks()) {
			final int index = books.size();
			final String realName = kindle.getRealBookName(s) == null ? s : kindle.getRealBookName(s);
			// Using the unicode ... here because it looks cooler in Code.
			final String name = (realName.length() >= 31 && kindle.getRealBookName(s) == null ? realName + '\u2026' : realName);
			books.add(new String[] { name, "\u2026" });
			threads.execute(new Runnable() {
				@Override
				public void run() {
					final Iterator<Story> i = Searches.parseFullSearchPartially(Util.FIMFICTION + "index.php?" + new SearchRequestBuilder().setSearchTerm(realName), connection, 0);
					if(i.hasNext()) {
						final Story story = i.next();
						onKindleModel.setValueAt("<html><font color=green>\u2026</font></html>", index, 1);
						try {
							Stories.updateStory(story, connection);
							if(kindle.getBookUpdateDateTotal(s) < story.getModifyTime().getTime())
								onKindleModel.setValueAt(arrow, index, 1);
							else
								onKindleModel.setValueAt("<html><font color=green>\u2714</font></html>", index, 1);
						} catch(Exception e) {
							e.printStackTrace();
						}
					} else {
						onKindleModel.setValueAt("<html><font color=red>\u2718</font></html>", index, 1);
					}
				}
			});
		}
		for(final String[] s : books) {
			onKindleModel.addRow(s);
		}
	}
	
	private void updateDownloads() {
		while(downloadedEbooksModel.getRowCount() > 0)
			downloadedEbooksModel.removeRow(0);
		final String[] books = dlManager.getDownloadDirectory().list(new FilenameFilter() {
			@Override
			public boolean accept(File f, String s) {
				return s.endsWith(".epub");
			}
		});
		if(books != null)
			for(final String s : books) {
				downloadedEbooksModel.addRow(new String[] { s });
			}
	}
}
