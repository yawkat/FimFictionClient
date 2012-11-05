package at.yawk.fimfiction.examples.control;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import at.yawk.fimfiction.Story;

public class StoryNameDisplay extends JPanel {
	private static final long	serialVersionUID	= 1L;
	
	public StoryNameDisplay(final Story s) {
		setLayout(new GridLayout(1, 0, 4, 0));
		add(new JLabel(s.getTitle()));
		final JLabel author = new JLabel(s.getAuthor().getName());
		author.setForeground(Color.GRAY);
		add(author);
		setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
	}
}
