package at.yawk.fimfiction;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import at.yawk.fimfiction.examples.control.IDownloadUpdate;

public final class Stories {
	private Stories() {
		
	}
	
	public static void downloadStory(final Story s, final OutputStream output, final EnumDownloadType downloadType, final IFimFictionConnection ffc, final IDownloadUpdate update) throws IOException {
		String inputUrl;
		switch(downloadType) {
		case EPUB:
			inputUrl = Util.FIMFICTION + "download_epub.php?story=" + s.getId();
			break;
		case HTML:
			inputUrl = Util.FIMFICTION + "download_story.php?html&story=" + s.getId();
			break;
		case TXT:
			inputUrl = Util.FIMFICTION + "download_story.php?story=" + s.getId();
			break;
		default:
			throw new NullPointerException("Unknown download type: " + downloadType);
		}
		final IURLConnection c = ffc.getConnection(new URL(inputUrl));
		Util.copyStream(c.getInputStream(), output, c.getContentLengthLong() == -1 ? null : update, c.getContentLengthLong());
	}
	
	public static void downloadStory(final Story s, final OutputStream output, final EnumDownloadType downloadType, final IFimFictionConnection ffc) throws IOException {
		downloadStory(s, output, downloadType, ffc, null);
	}
	
	public static boolean updateStory(final Story s, IFimFictionConnection ffc) throws MalformedURLException, IOException, JSONException {
		final JSONTokener jst = new JSONTokener(Util.readFully(ffc.getConnection(new URL(Util.FIMFICTION + "api/story.php?story=" + s.getId())).getInputStream()));
		final JSONObject nxt = (JSONObject)jst.nextValue();
		if(nxt.has("story")) {
			final JSONObject jso = ((JSONObject)nxt).getJSONObject("story");
			s.setTitle(jso.getString("title"));
			try {
				s.setShortDescription(jso.getString("short_description"));
				s.setDescription(jso.getString("description"));
			} catch(JSONException j) {
				// Tends to happen from time to time, not sure why.
				s.setShortDescription("");
				s.setDescription("");
			}
			s.setModifyTime(new Date(jso.getLong("date_modified") * 1000L));
			if(jso.has("image"))
				s.setImageLocation(jso.getString("image"));
			if(jso.has("full_image"))
				s.setFullImageLocation(jso.getString("full_image"));
			s.setViews(jso.getInt("views"));
			s.setTotalViews(jso.getInt("total_views"));
			s.setWords(jso.getInt("words"));
			s.setComments(jso.getInt("comments"));
			s.setStatus(EnumStoryStatus.parse(jso.getString("status")));
			s.setContentRating(EnumStoryContentRating.parse(jso.getString("content_rating_text")));
			s.setLikes(jso.getInt("likes"));
			s.setDislikes(jso.getInt("dislikes"));
			s.setAuthor(new Author(Integer.parseInt(jso.getJSONObject("author").getString("id"))));
			s.getAuthor().setName(jso.getJSONObject("author").getString("name"));
			if(jso.has("chapters")) {
				final JSONArray chapters = jso.getJSONArray("chapters");
				s.setChapters(new Chapter[Math.min(jso.getInt("chapter_count"), chapters.length())]);
				for(int i = 0; i < s.getChapters().length; i++) {
					final JSONObject cj = chapters.getJSONObject(i);
					final Chapter c = new Chapter(cj.getInt("id"), s, i);
					c.setTitle(cj.getString("title"));
					c.setWords(cj.getInt("words"));
					c.setViews(cj.getInt("views"));
					c.setModifyTime(new Date(cj.getLong("date_modified") * 1000L));
					s.getChapters()[i] = c;
				}
			} else {
				s.setChapters(new Chapter[0]);
			}
			return true;
		} else {
			return false;
		}
	}
	
	public static EnumCharacter[] getCharacters(final Story s, IFimFictionConnection ffc) throws MalformedURLException, IOException {
		final Document d = Util.getHTML(ffc.getConnection(new URL(Util.FIMFICTION + "story/" + s.getId())));
		final Elements es = d.getElementsByClass("character_icon");
		final EnumCharacter[] characters = new EnumCharacter[es.size() / 2];
		for(int i = 1; i < es.size(); i += 2) {
			characters[i / 2] = EnumCharacter.parseImageUrl("http:" + es.get(i).attr("src"));
		}
		return characters;
	}
}
