package at.yawk.fimfiction.examples.control;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import at.yawk.fimfiction.AccountActions;
import at.yawk.fimfiction.BBHelper;
import at.yawk.fimfiction.Chapter;
import at.yawk.fimfiction.EnumDownloadType;
import at.yawk.fimfiction.IFimFictionConnection;
import at.yawk.fimfiction.Stories;
import at.yawk.fimfiction.Story;

public class StoryDetailedInfo extends JPanel {
	private static final long			serialVersionUID	= 1L;
	
	private final JLabel				title;
	private final JLabel				description;
	private final JTable				chapters;
	private final DefaultTableModel		chaptersModel;
	
	private final IFimFictionConnection	connection;
	private final Story					story;
	private final DownloadManager		dlManager;
	private final EpubServer			server;
	
	public StoryDetailedInfo(final Story story, final IFimFictionConnection connection, final DownloadManager dlManager, final EpubServer server, final Runnable back) {
		this.dlManager = dlManager;
		this.server = server;
		this.connection = connection;
		
		final JPanel content = new JPanel();
		setLayout(new GridLayout(1, 1));
		
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		
		this.story = story;
		
		if(back != null) {
			final JButton dl = new JButton();
			dl.setAction(new AbstractAction() {
				private static final long	serialVersionUID	= 1L;
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					back.run();
				}
			});
			dl.setText("Back");
			content.add(dl);
		}
		
		title = new JLabel();
		title.setFont(getFont().deriveFont(16F).deriveFont(Font.BOLD));
		title.setText("Loading...");
		content.add(title);
		
		final JButton dl = new JButton();
		dl.setAction(new AbstractAction() {
			private static final long	serialVersionUID	= 1L;
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dlManager.download(story, connection);
			}
		});
		dl.setText("Download");
		content.add(dl);
		
		final JButton hideshow = new JButton();
		hideshow.setAction(new AbstractAction() {
			private static final long	serialVersionUID	= 1L;
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				description.setVisible(!description.isVisible());
				hideshow.setText((description.isVisible() ? "Hide" : "Show") + " description");
			}
		});
		hideshow.setText("Show description");
		content.add(hideshow);
		
		description = new JLabel();
		description.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
		description.setVisible(false);
		content.add(description);
		
		{
			chapters = new JTable(chaptersModel = new DefaultTableModel() {
				private static final long	serialVersionUID	= 1L;
				
				@Override
				public boolean isCellEditable(int rowIndex, int mColIndex) {
					return mColIndex < 2;
				}
				
				@Override
				public Class<?> getColumnClass(int c) {
					if(c == 0) {
						return JCheckBox.class;
					} else if(c == 1) {
						return JButton.class;
					}
					return super.getColumnClass(c);
				}
			});
			{
				final TableColumn tc = new TableColumn(0);
				tc.setHeaderValue("");
				chapters.addColumn(tc);
				chaptersModel.addColumn(tc.getHeaderValue());
			}
			{
				final TableColumn tc = new TableColumn(1);
				tc.setHeaderValue("");
				chapters.addColumn(tc);
				chaptersModel.addColumn(tc.getHeaderValue());
			}
			{
				final TableColumn tc = new TableColumn(2);
				tc.setHeaderValue("Title");
				chapters.addColumn(tc);
				chaptersModel.addColumn(tc.getHeaderValue());
			}
			final JScrollPane scrollpane = new JScrollPane(chapters);
			scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			scrollpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			scrollpane.getVerticalScrollBar().setUnitIncrement(scrollpane.getVerticalScrollBar().getUnitIncrement() * 10);
			scrollpane.setVisible(true);
			content.add(scrollpane);
			
			final class ButtonCellEdit extends AbstractCellEditor implements TableCellEditor {
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
			
			chapters.getColumnModel().getColumn(0).setMaxWidth(20);
			chapters.getColumnModel().getColumn(0).setCellRenderer(new TableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable arg0, Object arg1, boolean arg2, boolean arg3, int arg4, int arg5) {
					return (Component)arg1;
				}
			});
			chapters.getColumnModel().getColumn(0).setCellEditor(new ButtonCellEdit());
			
			chapters.getColumnModel().getColumn(1).setMaxWidth(35);
			chapters.getColumnModel().getColumn(1).setCellRenderer(new TableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable arg0, Object arg1, boolean arg2, boolean arg3, int arg4, int arg5) {
					return (Component)arg1;
				}
			});
			chapters.getColumnModel().getColumn(1).setCellEditor(new ButtonCellEdit());
		}
		
		if(story.getTitle() == null) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Stories.updateStory(story, connection);
						updateContent();
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		} else {
			updateContent();
		}
		
		add(content);
	}
	
	private void updateContent() {
		title.setText("<html>" + story.getTitle() + "</html>");
		description.setText("<html><p align='justify'>" + BBHelper.bbToHtml(story.getDescription(), true) + "</p></html>");
		for(final Chapter c : story.getChapters()) {
			final JCheckBox hasRead = new JCheckBox();
			hasRead.setMargin(new Insets(0, 0, 0, 0));
			hasRead.setEnabled(false);
			final JButton dl = new JButton();
			dl.setMargin(new Insets(0, 0, 0, 0));
			dl.setAction(new AbstractAction() {
				private static final long	serialVersionUID	= 1L;
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					final Desktop desktop = Desktop.getDesktop();
					
					if(desktop.isSupported(Desktop.Action.BROWSE)) {
						final File f = new File(dlManager.getDownloadDirectory(), story.getTitle().replaceAll("[^\\w ]", "") + "." + EnumDownloadType.EPUB.getFileType());
						if(!f.exists() || f.lastModified() <= story.getModifyTime().getTime())
							dlManager.downloadImmediatly(story, connection, EnumDownloadType.EPUB);
						if(!server.running())
							server.start();
						try {
							desktop.browse(new URI("http://127.0.0.1:" + server.getPort() + "/" + story.getTitle().replaceAll("[^\\w ]", "").replace(" ", "%20") + '.' + EnumDownloadType.EPUB.getFileType() + "/Chapter" + (c.getStoryIndex() + 1) + ".html"));
						} catch(IOException | URISyntaxException e) {
							e.printStackTrace();
						}
					}
				}
			});
			dl.setText("Open");
			chaptersModel.addRow(new Object[] { hasRead, dl, c.getTitle() });
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					final boolean[] ab = AccountActions.getHasRead(connection, story);
					for(int i = 0; i < ab.length; i++) {
						final JCheckBox cb = ((JCheckBox)chaptersModel.getValueAt(i, 0));
						cb.setEnabled(true);
						cb.setSelected(ab[i]);
						final int finalIndex = i;
						cb.setAction(new AbstractAction() {
							private static final long	serialVersionUID	= 1L;
							
							@Override
							public void actionPerformed(ActionEvent arg0) {
								try {
									cb.setSelected(AccountActions.toggleRead(connection, story.getChapters()[finalIndex]));
								} catch(IOException e) {
									e.printStackTrace();
								}
							}
						});
					}
					repaint();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
}
