package at.yawk.fimfiction.examples.control;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class EpubServerConfig extends JPanel implements ISelectionNotify {
	private static final long	serialVersionUID	= 1L;
	
	private final EpubServer	server;
	private final JButton		serverControl;
	
	public EpubServerConfig(final EpubServer server) {
		this.server = server;
		setLayout(new GridLayout(1, 1));
		final JPanel jp = new JPanel();
		jp.setLayout(new GridBagLayout());
		final JScrollPane sp = new JScrollPane(jp);
		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(sp);
		serverControl = new JButton();
		serverControl.setAction(new AbstractAction() {
			private static final long	serialVersionUID	= 1L;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(server.running()) {
					server.stop();
					serverControl.setText(server.running() ? "Stop" : "Start");
				} else {
					server.start();
					serverControl.setText(server.running() ? "Stop" : "Start");
				}
			}
		});
		serverControl.setText(server.running() ? "Stop" : "Start");
		final JSpinner port = new JSpinner(new SpinnerNumberModel(server.getPort(), 1, Short.MAX_VALUE, 1));
		port.setEditor(new JSpinner.NumberEditor(port, "#"));
		port.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				server.setPort((Integer)port.getValue());
			}
		});
		jp.add(serverControl, createConstraints(2, 0, true));
		jp.add(port, createConstraints(2, 0, true));
		final JCheckBox customStyle = new JCheckBox();
		customStyle.setAction(new AbstractAction() {
			private static final long	serialVersionUID	= 1L;
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				server.setUseCustomStylesheet(!server.useCustomStylesheet());
				customStyle.setSelected(server.useCustomStylesheet());
			}
		});
		customStyle.setSelected(server.useCustomStylesheet());
		jp.add(new JLabel("Custom Stylesheet"), createConstraints(1, 0, false));
		jp.add(customStyle, createConstraints(1, 1, true));
		

		final JCheckBox fixHTML = new JCheckBox();
		fixHTML.setAction(new AbstractAction() {
			private static final long	serialVersionUID	= 1L;
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				server.setFixHTML(!server.fixHTML());
				fixHTML.setSelected(server.fixHTML());
			}
		});
		fixHTML.setSelected(server.fixHTML());
		jp.add(new JLabel("Fix HTML"), createConstraints(1, 0, false));
		jp.add(fixHTML, createConstraints(1, 1, true));
	}
	
	private int	ycounter	= 0;
	
	private GridBagConstraints createConstraints(int width, int gridx, boolean newline) {
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gridx;
		gbc.gridy = ycounter;
		gbc.gridheight = 1;
		if(newline)
			ycounter += 1;
		gbc.gridwidth = width;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(2, 2, 2, 2);
		return gbc;
	}
	
	@Override
	public void select() {
		serverControl.setText(server.running() ? "Stop" : "Start");
	}
	
	@Override
	public void deselect() {}
}
