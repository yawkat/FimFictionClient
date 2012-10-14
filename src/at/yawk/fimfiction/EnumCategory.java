package at.yawk.fimfiction;

public enum EnumCategory {
	ROMANCE("category_romance"),
	TRAGEDY("category_tragedy"),
	SAD("category_sad"),
	DARK("category_dark"),
	COMEDY("category_comedy"),
	RANDOM("category_random"),
	CROSSOVER("category_crossover"),
	ADVENTURE("category_adventure"),
	SLICE_OF_LIFE("category_slice_of_life"),
	ALTERNATE_UNIVERSE("category_alternate_universe"),
	HUMAN("category_human");
	
	private final String searchValue;
	
	private EnumCategory(final String searchValue) {
		this.searchValue = searchValue;
	}
	
	public String getSearchValue() {
		return searchValue;
	}
}
