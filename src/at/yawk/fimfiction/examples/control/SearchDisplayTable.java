package at.yawk.fimfiction.examples.control;

import java.awt.GridLayout;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.json.JSONException;

import at.yawk.fimfiction.IFimFictionConnection;
import at.yawk.fimfiction.Searches;
import at.yawk.fimfiction.Stories;
import at.yawk.fimfiction.Story;
import at.yawk.fimfiction.StoryID;

public class SearchDisplayTable extends JPanel {
	private static final long				serialVersionUID	= 1L;
	
	private final Thread					updateThread;
	private final Map<Story, JComponent>	stories				= Collections.synchronizedMap(new LinkedHashMap<Story, JComponent>());
	private final JScrollPane				scrollpane;
	private final JPanel					mainPanel;
	
	private boolean							doneUpdating		= false;
	
	public SearchDisplayTable(final String request, final IFimFictionConnection connection) {
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		setLayout(new GridLayout(1, 1));
		mainPanel.setVisible(true);
		scrollpane = new JScrollPane(mainPanel);
		scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollpane.getVerticalScrollBar().setUnitIncrement(scrollpane.getVerticalScrollBar().getUnitIncrement() * 5);
		scrollpane.setVisible(true);
		add(scrollpane);
		updateData();
		updateThread = new Thread() {
			@Override
			public void run() {
				final Iterator<Story> stories = Searches.parseFullSearchPartially(request, connection, 0);
				final Set<Story> allStories = new HashSet<>(SearchDisplayTable.this.stories.size() == 0 ? 16 : SearchDisplayTable.this.stories.size());
				while(!isInterrupted() && stories.hasNext()) {
					final Story s = new StoryID(stories.next());
					if(!SearchDisplayTable.this.stories.containsKey(s)) {
						try {
							Stories.updateStory(s, connection);
						} catch(IOException | JSONException e) {
							e.printStackTrace();
						}
						SearchDisplayTable.this.stories.put(s, new StoryNameDisplay(s));
						updateData();
					}
					allStories.add(s);
				}
				doneUpdating = true;
			}
		};
		setVisible(true);
	}
	
	private void updateData() {
		for(final Map.Entry<Story, JComponent> entry : stories.entrySet()) {
			if(!Arrays.asList(mainPanel.getComponents()).contains(entry.getValue())) {
				mainPanel.add(entry.getValue());
			}
		}
		mainPanel.revalidate();
	}
	
	public void startUpdating() {
		if(!updateThread.isAlive() && !doneUpdating)
			updateThread.start();
	}
	
	public void stopUpdating() {
		if(updateThread.isAlive())
			updateThread.interrupt();
	}
	
	public void refresh() {
		doneUpdating = false;
		stories.clear();
		startUpdating();
	}
}
