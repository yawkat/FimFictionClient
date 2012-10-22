package at.yawk.fimfiction.examples.control;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.EnumMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import at.yawk.fimfiction.EnumCharacter;
import at.yawk.fimfiction.EnumSearchOrder;
import at.yawk.fimfiction.SearchRequestBuilder;

public class SearchBuilderDisplay extends JPanel {
	private static final long	serialVersionUID	= 1L;
	
	public SearchBuilderDisplay(final SearchRequestBuilder builder) {
		final GridBagLayout gl = new GridBagLayout();
		setLayout(gl);
		final JTextField searchString = addAndStandardText(new JTextField(), "Search string");
		final JComboBox<EnumSearchOrder> searchOrder;
		{
			searchOrder = new JComboBox<>(EnumSearchOrder.values());
			add(searchOrder, createConstraints(1));
		}
		final CharacterSelector[] characters;
		{
			final GridBagLayout gl1 = new GridBagLayout();
			final JPanel panel = new JPanel(gl1);
			characters = new CharacterSelector[EnumCharacter.values().length];
			int pCounter = 0;
			for(int i = 0; i < characters.length; i++) {
				characters[i] = new CharacterSelector(EnumCharacter.values()[i]);
				final GridBagConstraints c = new GridBagConstraints();
				c.gridx = pCounter % 9;
				c.gridy = pCounter / 9;
				pCounter += c.gridwidth = EnumCharacter.values()[i].getImageWidth() / 32;
				c.gridheight = 1;
				c.insets = new Insets(1, 1, 1, 1);
				panel.add(characters[i], c);
			}
			add(panel, createConstraints(1));
		}
		{
			final JButton search = new JButton(new AbstractAction() {
				private static final long	serialVersionUID	= 1L;
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					builder.setSearchTerm(searchString.getText());
					builder.setSearchOrder((EnumSearchOrder)searchOrder.getSelectedItem());
					final EnumMap<EnumCharacter, Boolean> charactersMap = new EnumMap<>(EnumCharacter.class);
					for(final CharacterSelector cs : characters) {
						if(cs.isSelected != null)
							charactersMap.put(cs.character, cs.isSelected);
					}
					builder.setCharacters(charactersMap);
				}
			});
			search.setText("Search");
			add(search, createConstraints(1));
		}
		setVisible(true);
	}
	
	private JTextField addAndStandardText(final JTextField textField, final String text) {
		final AtomicBoolean isShowingGreyString = new AtomicBoolean(false);
		if(!textField.hasFocus()) {
			textField.setText(text);
			textField.setForeground(Color.GRAY);
			isShowingGreyString.set(true);
		}
		textField.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				if(textField.getText().length() == 0) {
					textField.setText(text);
					textField.setForeground(Color.GRAY);
					isShowingGreyString.set(true);
				} else {
					textField.setForeground(Color.BLACK);
					isShowingGreyString.set(false);
				}
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
				if(isShowingGreyString.get())
					textField.setText("");
				textField.setForeground(Color.BLACK);
				isShowingGreyString.set(false);
			}
		});
		textField.setColumns(20);
		textField.setVisible(true);
		add(textField, createConstraints(1));
		return textField;
	}
	
	private static BufferedImage	selected	= null;
	private static BufferedImage	unselected	= null;
	static {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					selected = ImageIO.read(new URL("http://static.fimfiction.net/images/selected/include.png"));
					unselected = ImageIO.read(new URL("http://static.fimfiction.net/images/selected/exclude.png"));
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	private class CharacterSelector extends JButton {
		
		private static final long	serialVersionUID	= 1L;
		private final EnumCharacter	character;
		private Boolean				isSelected			= null;
		
		public CharacterSelector(EnumCharacter character) {
			this.character = character;
			setPreferredSize(new Dimension(character.getImageWidth(), 32));
			addActionListener(new AbstractAction() {
				private static final long	serialVersionUID	= 1L;
				
				@Override
				public void actionPerformed(ActionEvent e) {
					if(isSelected == null)
						isSelected = true;
					else if(isSelected)
						isSelected = false;
					else
						isSelected = null;
				}
			});
			setToolTipText(character.getDisplayName());
			setVisible(true);
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			final BufferedImage image = character.getImage();
			if(image != null) {
				g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
				if(isSelected == null) {
					g.setColor(new Color(0xff, 0xff, 0xff, 128));
					g.fillRect(0, 0, getWidth(), getHeight());
				} else if(isSelected) {
					if(selected != null) {
						g.drawImage(selected, getWidth() - selected.getWidth(), getHeight() - selected.getHeight(), selected.getWidth(), selected.getHeight(), null);
					}
				} else {
					if(unselected != null) {
						g.drawImage(unselected, getWidth() - unselected.getWidth(), getHeight() - unselected.getHeight(), unselected.getWidth(), unselected.getHeight(), null);
					}
				}
			} else {
				character.addOnLoad(new Runnable() {
					@Override
					public void run() {
						repaint();
					}
				});
			}
		}
	}
	
	private GridBagConstraints createConstraints(int height) {
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = ycounter;
		ycounter += gbc.gridheight = height;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(2, 2, 2, 2);
		return gbc;
	}
	
	private int	ycounter	= 0;
}
