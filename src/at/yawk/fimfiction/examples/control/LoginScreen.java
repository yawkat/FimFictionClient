package at.yawk.fimfiction.examples.control;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import at.yawk.fimfiction.FimFictionConnectionAccount;

public class LoginScreen extends JPanel {
	private static final long					serialVersionUID	= 1L;
	private final FimFictionConnectionAccount	connection;
	
	public LoginScreen(final FimFictionConnectionAccount connection) {
		this.connection = connection;
		setLayout(new BorderLayout());
		{
			final JLabel jl = new JLabel("Login");
			jl.setVisible(true);
			add(jl, BorderLayout.PAGE_START);
		}
		final JTextField username;
		{
			username = new JTextField();
			final AtomicBoolean isShowingGrey = new AtomicBoolean(false);
			username.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent arg0) {
					if(username.getText().length() == 0) {
						username.setText("username");
						username.setForeground(Color.GRAY);
						isShowingGrey.set(true);
					} else {
						username.setForeground(Color.BLACK);
						isShowingGrey.set(false);
					}
				}
				
				@Override
				public void focusGained(FocusEvent arg0) {
					if(isShowingGrey.get())
						username.setText("");
					isShowingGrey.set(false);
				}
			});
			username.setColumns(20);
			username.setVisible(true);
			add(username, BorderLayout.CENTER);
		}
		final JPasswordField password;
		{
			password = new JPasswordField("password");
			final AtomicBoolean isShowingGrey = new AtomicBoolean(true);
			password.setForeground(Color.GRAY);
			password.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent arg0) {
					if(password.getPassword().length == 0) {
						password.setText("password");
						password.setForeground(Color.GRAY);
						isShowingGrey.set(true);
					} else {
						password.setForeground(Color.BLACK);
						isShowingGrey.set(false);
					}
				}
				
				@Override
				public void focusGained(FocusEvent arg0) {
					if(isShowingGrey.get())
						password.setText("");
					isShowingGrey.set(false);
				}
			});
			password.setColumns(20);
			password.setVisible(true);
			add(password, BorderLayout.PAGE_END);
		}
		setVisible(true);
	}
}
