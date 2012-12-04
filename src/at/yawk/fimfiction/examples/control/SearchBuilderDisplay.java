package at.yawk.fimfiction.examples.control;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import at.yawk.fimfiction.EnumCategory;
import at.yawk.fimfiction.EnumCharacter;
import at.yawk.fimfiction.EnumSearchOrder;
import at.yawk.fimfiction.EnumStoryContentRating;
import at.yawk.fimfiction.EnumStoryMatureCategories;
import at.yawk.fimfiction.SearchRequestBuilder;

public class SearchBuilderDisplay extends JPanel {
	private static final long	serialVersionUID			= 1L;
	
	private static final byte	CHARACTER_SELECTION_ROWS	= 10;
	
	@SuppressWarnings("unchecked")
	public SearchBuilderDisplay(final SearchRequestBuilder builder, final Runnable onSubmit) {
		final GridBagLayout gl = new GridBagLayout();
		setLayout(gl);
		final JTextField searchString = addAndStandardText(new JTextField(), "Search string");
		final JComboBox<EnumSearchOrder> searchOrder;
		{
			searchOrder = new JComboBox<>(EnumSearchOrder.values());
			final JLabel jl = new JLabel("Search Order");
			jl.setLabelFor(searchOrder);
			add(jl, createConstraints(1, 0, false));
			add(searchOrder, createConstraints(1, 1, true));
		}
		final JComboBox<EnumStoryContentRating> contentRating;
		{
			contentRating = new JComboBox<EnumStoryContentRating>();
			for(final EnumStoryContentRating escr : EnumStoryContentRating.values())
				contentRating.addItem(escr);
			final JLabel jl = new JLabel("Content Rating");
			jl.setLabelFor(contentRating);
			add(jl, createConstraints(1, 0, false));
			add(contentRating, createConstraints(1, 1, true));
		}
		final JComboBox<EnumStoryMatureCategories> matureCategory;
		{
			matureCategory = new JComboBox<EnumStoryMatureCategories>();
			for(final EnumStoryMatureCategories esmc : EnumStoryMatureCategories.values())
				matureCategory.addItem(esmc);
			final JLabel jl = new JLabel("Mature Category");
			jl.setLabelFor(matureCategory);
			add(jl, createConstraints(1, 0, false));
			add(matureCategory, createConstraints(1, 1, true));
		}
		final JCheckBox completeOnly;
		{
			completeOnly = new JCheckBox();
			final JLabel jl = new JLabel("Completed");
			jl.setLabelFor(completeOnly);
			add(jl, createConstraints(1, 0, false));
			add(completeOnly, createConstraints(1, 1, true));
		}
		final JSpinner minimumWords;
		{
			minimumWords = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
			final JLabel jl = new JLabel("Minimum Words");
			jl.setLabelFor(minimumWords);
			add(jl, createConstraints(1, 0, false));
			add(minimumWords, createConstraints(1, 1, true));
		}
		final JSpinner maximumWords;
		{
			maximumWords = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
			final JLabel jl = new JLabel("Maximum Words");
			jl.setLabelFor(maximumWords);
			add(jl, createConstraints(1, 0, false));
			add(maximumWords, createConstraints(1, 1, true));
		}
		final JComboBox<String>[] categories;
		{
			categories = new JComboBox[EnumCategory.values().length];
			final GridLayout gl1 = new GridLayout(0, 2);
			gl1.setHgap(4);
			final JPanel panel = new JPanel(gl1);
			for(int i = 0; i < categories.length; i++) {
				categories[i] = new JComboBox<String>();
				categories[i].addItem("-");
				categories[i].addItem("\u2714");
				categories[i].addItem("\u2718");
				final JPanel display = new JPanel(new BorderLayout());
				final JLabel jl = new JLabel(EnumCategory.values()[i].toString());
				jl.setLabelFor(categories[i]);
				display.add(jl, BorderLayout.LINE_START);
				display.add(categories[i], BorderLayout.LINE_END);
				panel.add(display);
			}
			add(panel, createConstraints(2, 0, true));
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
				c.gridx = pCounter % CHARACTER_SELECTION_ROWS;
				c.gridy = pCounter / CHARACTER_SELECTION_ROWS;
				pCounter += c.gridwidth = EnumCharacter.values()[i].getImageWidth() / 32;
				c.gridheight = 1;
				c.insets = new Insets(1, 1, 1, 1);
				panel.add(characters[i], c);
			}
			add(panel, createConstraints(2, 0, true));
		}
		{
			final JButton search = new JButton(new AbstractAction() {
				private static final long	serialVersionUID	= 1L;
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(!greys.get(searchString).get())
						builder.setSearchTerm(searchString.getText());
					builder.setSearchOrder((EnumSearchOrder)searchOrder.getSelectedItem());
					final EnumMap<EnumCharacter, Boolean> charactersMap = new EnumMap<>(EnumCharacter.class);
					for(final CharacterSelector cs : characters) {
						if(cs.isSelected != null)
							charactersMap.put(cs.character, cs.isSelected);
					}
					builder.setCharacters(charactersMap);
					final EnumMap<EnumCategory, Boolean> categoryMap = new EnumMap<>(EnumCategory.class);
					for(int i = 0; i < categories.length; i++) {
						if(categories[i].getSelectedIndex() > 0)
							categoryMap.put(EnumCategory.values()[i], categories[i].getSelectedIndex() == 1);
					}
					builder.setCategories(categoryMap);
					builder.setContentRating((EnumStoryContentRating)contentRating.getSelectedItem());
					builder.setMatureCategories((EnumStoryMatureCategories)matureCategory.getSelectedItem());
					builder.setMustBeCompleted(completeOnly.isSelected());
					builder.setMinimumWords((Integer)minimumWords.getValue() == 0 ? null : (Integer)minimumWords.getValue());
					builder.setMaximumWords((Integer)maximumWords.getValue() == 0 ? null : (Integer)maximumWords.getValue());
					onSubmit.run();
				}
			});
			search.setText("Search");
			add(search, createConstraints(2, 0, true));
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
		greys.put(textField, isShowingGreyString);
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
		add(textField, createConstraints(2, 0, true));
		return textField;
	}
	
	private final Map<JTextField, AtomicBoolean>	greys		= new HashMap<>();
	
	private static BufferedImage					selected	= null;
	private static BufferedImage					unselected	= null;
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
	
	private int	ycounter	= 0;
}
