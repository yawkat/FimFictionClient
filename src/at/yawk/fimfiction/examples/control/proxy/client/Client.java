package at.yawk.fimfiction.examples.control.proxy.client;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import at.yawk.fimfiction.examples.control.Main;

public class Client {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			
		}
		new Main(new ProxyWebProvider(JOptionPane.showInputDialog("Proxy IP"), Integer.parseInt(JOptionPane.showInputDialog("Proxy Port")))).run();
	}
}
