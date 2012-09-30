package at.yawk.fimfiction;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class Story {
	private final int	id;
	private String		author		= null;
	private String		description	= null;
	private String		title		= null;
	private final Lock	lock		= new ReentrantLock();
	
	public Story(final int id) {
		this.id = id;
	}
	
	public final int getId() {
		return id;
	}
	
	@Override
	public int hashCode() {
		return id;
	}
	
	@Override
	public boolean equals(final Object o) {
		return o instanceof Story && o.hashCode() == this.hashCode();
	}
	
	public final String getDescription() {
		lock.lock();
		try {
			return description;
		} finally {
			lock.unlock();
		}
	}
	
	public final void setDescription(final String description) {
		lock.lock();
		try {
			this.description = description;
		} finally {
			lock.unlock();
		}
	}
	
	public final String getAuthor() {
		lock.lock();
		try {
			return author;
		} finally {
			lock.unlock();
		}
	}
	
	public final void setAuthor(final String author) {
		lock.lock();
		try {
			this.author = author;
		} finally {
			lock.unlock();
		}
	}
	
	public final void setTitle(final String title) {
		lock.lock();
		try {
			this.title = title;
		} finally {
			lock.unlock();
		}
	}
	
	public final String getTitle() {
		lock.lock();
		try {
			return title;
		} finally {
			lock.unlock();
		}
	}
}
