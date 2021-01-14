package i5.las2peer.services.mentoringCockpitService.Suggestion;

import java.util.ArrayList;

public class TextFormatter {
	
	public static String createHyperlink(String text, String url) {
		return "<a href=' " + url + "'>" + text + "</a>";
	}
	
	public static String createList(ArrayList<String> itemlist) {
		String res = "<ul>";
		for (String item : itemlist) {
			res = res + "<li>" + item + "</li>";
		}
		return res + "</ul>";
	}
	
	public static String quote(String text) {
		return "\"" + text + "\"";
	}
}
