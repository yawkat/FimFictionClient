package at.yawk.fimfiction;

public class Author {
	private final int	id;
	private String		name;
	
	public Author(final int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public boolean equals(Object o) {
		return o instanceof Author && ((Author)o).id == id && ((Author)o).name == name;
	}
	
	public int hashCode() {
		return (int)(((long)id + (name == null ? 0 : name.hashCode())) % Integer.MAX_VALUE);
	}
}
