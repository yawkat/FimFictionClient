package at.yawk.fimfiction;

public enum EnumCategory {
	ROMANCE("category_romance", "Romance"),
	TRAGEDY("category_tragedy", "Tragedy"),
	SAD("category_sad", "Sad"),
	DARK("category_dark", "Dark"),
	COMEDY("category_comedy", "Comedy"),
	RANDOM("category_random", "Random"),
	CROSSOVER("category_crossover", "Crossover"),
	ADVENTURE("category_adventure", "Adventure"),
	SLICE_OF_LIFE("category_slice_of_life", "Slice of Life"),
	ALTERNATE_UNIVERSE("category_alternate_universe", "Alternate Universe"),
	HUMAN("category_human", "Human");
	
	private final String searchValue;
	private final String toString;
	
	private EnumCategory(final String searchValue, final String toString) {
		this.searchValue = searchValue;
		this.toString = toString;
	}
	
	public String getSearchValue() {
		return searchValue;
	}
	
	public String toString() {
		return toString;
	}
	
	public static EnumCategory parse(String s) {
		s = "category_" + s;
		for(EnumCategory e : values())
			if(e.searchValue.equals(s))
				return e;
		return null;
	}
}
