package at.yawk.fimfiction.examples.control;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import at.yawk.fimfiction.FimFictionConnectionAccount;

public class Main implements Runnable {
	private final JFrame						mainFrame		= new JFrame("FimFiction Client");
	public final JComponent						mainContentBox	= new JPanel();
	private final FimFictionConnectionAccount	connection		= new FimFictionConnectionAccount();
	
	public static void main(String[] args) {
		new Main().run();
	}
	
	@Override
	public void run() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			
		}
		
		mainFrame.setLayout(new BorderLayout());
		mainContentBox.setPreferredSize(new Dimension(300, 500));
		mainFrame.add(mainContentBox, "Center");
		mainContentBox.setVisible(true);
		mainFrame.validate();
		mainFrame.pack();
		mainFrame.setLocationRelativeTo(null);
		mainFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent we) {
				System.exit(0);
			}
		});
		displayLoginScreen();
		mainFrame.setVisible(true);
	}
	
	private void displayLoginScreen() {
		mainContentBox.removeAll();
		mainContentBox.add(new LoginScreen(connection, new Runnable() {
			@Override
			public void run() {
				System.out.println("Logged in!");
				displayMainScreen();
			}
		}));
	}
	
	private void displayMainScreen() {
		mainContentBox.removeAll();
	}
}
