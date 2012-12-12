package at.yawk.fimfiction.examples.backup;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;

public class SearchGui implements Runnable {
	public static void main(String[] args) {
		new SearchGui().run();
	}
	
	@Override
	public void run() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		final JFrame mainFrame = new JFrame("Downloaded Stories");
		final JPanel searchResultContentPanel = new JPanel();
		final JScrollPane searchResultPane = new JScrollPane(searchResultContentPanel);
		final JTextField serchTermTextField = new JTextField();
		final JSpinner minimumWordCountSpinner = new JSpinner();
		
		//searchResultContentPanel.setPreferredSize(new Dimension(400, 700));
		searchResultContentPanel.setVisible(true);
		
		searchResultPane.setBounds(0, 20, 400, 600);
		searchResultPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		searchResultPane.setVisible(true);
		
		serchTermTextField.setBounds(2, 2, 300, 20);
		serchTermTextField.setVisible(true);
		
		final JSpinner.NumberEditor ne = new JSpinner.NumberEditor(minimumWordCountSpinner, "0");
		minimumWordCountSpinner.setEditor(ne);
		minimumWordCountSpinner.setBounds(2, 24, 300, 20);
		minimumWordCountSpinner.setVisible(true);
		
		mainFrame.add(serchTermTextField);
		mainFrame.add(minimumWordCountSpinner);
		mainFrame.add(searchResultPane);
		mainFrame.pack();
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setVisible(true);
	}
}
