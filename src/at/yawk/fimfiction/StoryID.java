package at.yawk.fimfiction;

public class StoryID extends Story {

	public StoryID(int id) {
		super(id);
	}
	
	public StoryID(Story s) {
		super(s.getId());
	}
	
	public boolean equals(final Object o) {
		return o instanceof Story && ((Story)o).getId() == this.getId();
	}
	
	public int hashCode() {
		return getId();
	}
}
