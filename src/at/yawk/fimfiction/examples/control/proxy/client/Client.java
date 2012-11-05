package at.yawk.fimfiction.examples.control.proxy.client;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import at.yawk.fimfiction.EnumCharacter;
import at.yawk.fimfiction.IWebProvider;
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
		final IWebProvider web = new ProxyWebProvider(JOptionPane.showInputDialog("Proxy IP"), Integer.parseInt(JOptionPane.showInputDialog("Proxy Port")));
		EnumCharacter.setSpecialWebProvider(web);
		new Main(web).run();
	}
}
