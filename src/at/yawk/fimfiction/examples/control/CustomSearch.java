package at.yawk.fimfiction.examples.control;

import javax.swing.JPanel;

import at.yawk.fimfiction.IFimFictionConnection;
import at.yawk.fimfiction.SearchRequestBuilder;

public class CustomSearch extends JPanel {
	private static final long	serialVersionUID	= 1L;
	
	public CustomSearch(final IFimFictionConnection connection) {
		add(new SearchBuilderDisplay(new SearchRequestBuilder()));
	}
}
