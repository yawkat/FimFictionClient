package at.yawk.fimfiction.examples.control;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import at.yawk.fimfiction.FimFictionConnectionAccount;
import at.yawk.fimfiction.IWebProvider;
import at.yawk.fimfiction.StandardInternetProvider;

public class Main implements Runnable {
	private final JFrame						mainFrame		= new JFrame("FimFiction Client");
	public final JComponent						mainContentBox	= new JPanel();
	private final FimFictionConnectionAccount	connection;
	
	public Main(final IWebProvider web) {
		connection = new FimFictionConnectionAccount(web);
	}
	
	public static void main(String[] args) {
		Main m = new Main(new StandardInternetProvider());
		if(args.length == 2)
			m.connection.login(args[0], args[1]);
		m.run();
	}
	
	@Override
	public void run() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			
		}
		
		mainFrame.setLayout(new GridLayout(1, 1));
		mainContentBox.setPreferredSize(new Dimension(500, 700));
		mainFrame.add(mainContentBox);
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
		if(connection.isLoggedIn())
			displayMainScreen();
		else
			displayLoginScreen();
		mainFrame.setVisible(true);
	}
	
	public void displayLoginScreen() {
		mainContentBox.removeAll();
		mainContentBox.setLayout(new FlowLayout(FlowLayout.CENTER));
		mainContentBox.add(new LoginScreen(connection, new Runnable() {
			@Override
			public void run() {
				System.out.println("Logged in!");
				displayMainScreen();
			}
		}));
		mainContentBox.revalidate();
		mainContentBox.repaint();
	}
	
	public void displayMainScreen() {
		mainContentBox.removeAll();
		mainContentBox.setLayout(new GridLayout(1, 1));
		mainContentBox.add(new MainScreen(connection, this));
		mainContentBox.revalidate();
	}
}
