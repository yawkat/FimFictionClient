package at.yawk.fimfiction.examples.control;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import at.yawk.fimfiction.FimFictionConnectionAccount;

public class LoginScreen extends JPanel {
	private static final long	serialVersionUID	= 1L;
	
	public LoginScreen(final FimFictionConnectionAccount connection, final Runnable onLogin) {
		final JTextField username = new JTextField();
		final JPasswordField password = new JPasswordField();
		final JButton login = new JButton();
		final AtomicBoolean isShowingGreyUsername = new AtomicBoolean(false);
		final AtomicBoolean isShowingGreyPassword = new AtomicBoolean(false);
		final Action loginAction = new AbstractAction() {
			private static final long	serialVersionUID	= 1L;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				login.setEnabled(false);
				new Thread(new Runnable() {
					@Override
					public void run() {
						if(!isShowingGreyUsername.get() && !isShowingGreyPassword.get()) {
							if(connection.login(username.getText(), new String(password.getPassword()))) {
								onLogin.run();
							} else {
								login.setEnabled(true);
								JOptionPane.showMessageDialog(LoginScreen.this, "Bad Login");
							}
						} else {
							login.setEnabled(true);
							JOptionPane.showMessageDialog(LoginScreen.this, "Missing information");
						}
					}
				}).start();
			}
		};
		
		final GridLayout gl = new GridLayout(3, 1);
		gl.setVgap(2);
		setLayout(gl);
		{
			if(!username.hasFocus()) {
				username.setText("username");
				username.setForeground(Color.GRAY);
				isShowingGreyUsername.set(true);
			}
			username.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent arg0) {
					if(username.getText().length() == 0) {
						username.setText("username");
						username.setForeground(Color.GRAY);
						isShowingGreyUsername.set(true);
					} else {
						username.setForeground(Color.BLACK);
						isShowingGreyUsername.set(false);
					}
				}
				
				@Override
				public void focusGained(FocusEvent arg0) {
					if(isShowingGreyUsername.get())
						username.setText("");
					username.setForeground(Color.BLACK);
					isShowingGreyUsername.set(false);
				}
			});
			username.setColumns(20);
			username.setVisible(true);
			username.setAction(loginAction);
			add(username);
		}
		{
			if(!password.hasFocus()) {
				password.setText("password");
				password.setForeground(Color.GRAY);
				isShowingGreyPassword.set(true);
			}
			password.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent arg0) {
					if(password.getPassword().length == 0) {
						password.setText("password");
						password.setForeground(Color.GRAY);
						isShowingGreyPassword.set(true);
					} else {
						password.setForeground(Color.BLACK);
						isShowingGreyPassword.set(false);
					}
				}
				
				@Override
				public void focusGained(FocusEvent arg0) {
					if(isShowingGreyPassword.get())
						password.setText("");
					password.setForeground(Color.BLACK);
					isShowingGreyPassword.set(false);
				}
			});
			password.setColumns(20);
			password.setVisible(true);
			password.setAction(loginAction);
			add(password);
		}
		{
			login.setAction(loginAction);
			login.setText("Login");
			login.setVisible(true);
			add(login);
		}
		setVisible(true);
	}
}
