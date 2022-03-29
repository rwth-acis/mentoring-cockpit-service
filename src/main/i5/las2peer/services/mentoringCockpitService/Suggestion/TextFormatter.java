package i5.las2peer.services.mentoringCockpitService.Suggestion;

import java.util.ArrayList;

public class TextFormatter {
	
	public static String createHyperlink(String text, String url) {
		return " click on this Link to access it! " + url + text+ "\r\n";
	}
	
	public static String createList(ArrayList<String> itemlist) {
		String res = "";
		for (String item : itemlist) {
			res = res + "\r\n" + item + "";
		}
		return res + "";
	}
	
	public static String createOrderedList(ArrayList<String> itemlist) {
		String res = "<ol>";
		for (String item : itemlist) {
			res = res + "<li>" + item + "</li>";
		}
		return res + "</ol>";
	}
	
	public static String quote(String text) {
		return "\"" + text + "\"";
	}

	public static String emotion(Emotion emotion){
		switch (emotion) {
			case UNDEFINED:
				return "Here are some items for you to work on: ";
			case HAPPY:
				return "You seem to be happy :smile:, take advantage of it and try to work on these items: ";
			case SAD:
				return "You seem to be a little down :worried: , maybe try something easier, like these items: ";
			case PS:  
				return "You seem exited :star_struck: , challenge yourself with the following items: "; 
			case ANGRY: 
				return "You seem angry :angry: , maybe try something less demanding, like, these items: ";
			default:
				return "|Error|";
			}
	}
}
