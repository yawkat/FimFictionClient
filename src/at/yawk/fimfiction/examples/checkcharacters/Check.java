package at.yawk.fimfiction.examples.checkcharacters;

import java.io.IOException;
import java.net.URL;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import at.yawk.fimfiction.EnumCharacter;
import at.yawk.fimfiction.StandardInternetProvider;
import at.yawk.fimfiction.Util;

public class Check {
	public static void main(String[] args) throws IOException {
		final Document d = Util.getHTML(new StandardInternetProvider().getConnection(new URL(Util.FIMFICTION + "index.php?view=category")));
		final Elements characters = d.getElementsByClass("select_character");
		main: for(Element character : characters) {
			final int id = Integer.parseInt(character.getElementsByTag("input").first().val());
			for(EnumCharacter ec : EnumCharacter.values()) {
				if(ec.getId() == id)
					continue main;
			}
			final Element image = character.getElementsByTag("img").first();
			final String name = image.attr("title");
			final String imgurl;
			{
				String s = image.attr("style");
				s = s.substring(s.indexOf("url(") + 4, s.lastIndexOf(')'));
				if(s.charAt(0) == '\'')
					s = s.substring(1, s.length() - 1);
				if(s.startsWith("//"))
					s = "http:" + s;
				imgurl = s;
			}
			System.out.println(name.replace(' ', '_').toUpperCase() + "(" + id + ", \"" + name + "\", \"" + imgurl + "\"),");
		}
	}
}
