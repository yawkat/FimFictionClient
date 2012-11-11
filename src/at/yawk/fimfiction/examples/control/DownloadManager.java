package at.yawk.fimfiction.examples.control;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import at.yawk.fimfiction.EnumDownloadType;
import at.yawk.fimfiction.IFimFictionConnection;
import at.yawk.fimfiction.Stories;
import at.yawk.fimfiction.Story;

public class DownloadManager {
	private final Executor						downloader		= Executors.newFixedThreadPool(5);
	private EnumDownloadType					downloadType	= EnumDownloadType.EPUB;
	private File								downloadDir		= new File("downloads");
	private final Collection<IDownloadListener>	listeners		= new HashSet<>();
	private final Map<String, Story>				stories			= new HashMap<>();
	
	public void setDownloadDirectory(final File dir) {
		this.downloadDir = dir;
	}
	
	public void setStandardDownloadType(final EnumDownloadType downloadType) {
		this.downloadType = downloadType;
	}
	
	public void download(final Story story, final IFimFictionConnection ffc, final EnumDownloadType downloadType) {
		final File dir = downloadDir;
		final Collection<IDownloadUpdate> listeners = new HashSet<>();
		for(final IDownloadListener dl : this.listeners)
			if(dl != null) {
				final IDownloadUpdate du = dl.getDownloadUpdate(story);
				if(dl != null)
					listeners.add(du);
			}
		stories.put(new File(dir, story.getTitle().replaceAll("[^\\w ]", "") + '.' + downloadType.getFileType()).getAbsolutePath(), story);
		downloader.execute(new Runnable() {
			@Override
			public void run() {
				try {
					if(!dir.exists())
						dir.mkdirs();
					if(story.getTitle() == null)
						Stories.updateStory(story, ffc);
					final OutputStream fo = new FileOutputStream(new File(dir, story.getTitle().replaceAll("[^\\w ]", "") + '.' + downloadType.getFileType()));
					Stories.downloadStory(story, fo, downloadType, ffc, new IDownloadUpdate() {
						@Override
						public void setProgress(float progress) {
							for(IDownloadUpdate du : listeners)
								du.setProgress(progress);
						}
					});
					fo.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void downloadImmediatly(final Story story, final IFimFictionConnection ffc, final EnumDownloadType downloadType) {
		final File dir = downloadDir;
		final Collection<IDownloadUpdate> listeners = new HashSet<>();
		for(final IDownloadListener dl : this.listeners)
			if(dl != null) {
				final IDownloadUpdate du = dl.getDownloadUpdate(story);
				if(dl != null)
					listeners.add(du);
			}
		try {
			if(!dir.exists())
				dir.mkdirs();
			if(story.getTitle() == null)
				Stories.updateStory(story, ffc);
			final OutputStream fo = new FileOutputStream(new File(dir, story.getTitle().replaceAll("[^\\w ]", "") + '.' + downloadType.getFileType()));
			Stories.downloadStory(story, fo, downloadType, ffc, new IDownloadUpdate() {
				@Override
				public void setProgress(float progress) {
					for(IDownloadUpdate du : listeners)
						du.setProgress(progress);
				}
			});
			fo.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addListener(final IDownloadListener listener) {
		listeners.add(listener);
	}
	
	public void download(final Story story, final IFimFictionConnection ffc) {
		download(story, ffc, this.downloadType);
	}
	
	public static interface IDownloadListener {
		public IDownloadUpdate getDownloadUpdate(Story story);
	}
	
	public EnumDownloadType getStandardDownloadType() {
		return downloadType;
	}
	
	public File getDownloadDirectory() {
		return downloadDir;
	}
	
	public Story getStoryForFile(final File f) {
		return stories.get(f.getAbsolutePath());
	}
}
