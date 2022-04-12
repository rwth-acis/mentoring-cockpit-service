package i5.las2peer.services.mentoringCockpitService.Suggestion;

import java.util.ArrayList;

public class TextFormatter {
	
	public static String createHyperlink(String text, String url) {
		//For HTML format in chat interfaces:  return "<a href=' " + url + "'>" + text + "</a>";
		return "["+text+"]"+"("+url+")";
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

	public static String emotion(Emotion emotion, double valence, String text){
		switch (emotion) {
			case UNDEFINED:
				return "Here are some items for you to work on: ";
			case HAPPY:
				if (valence > 0.9)
				{
					return "You seem to be feeling it! :sunglasses: , here is a challenge for you!   "+ "\r\n"+ text;
				}
				return "You seem to be feeling happy :smile:, take advantage of it, challenge yourself try to work on these items:   " + "\r\n"+ text;
			case SAD:
				if (valence < -0.9)
				{
					return "You seem to be feeling sad :confused: , consider taking a short break :person_in_lotus_position: and comming back, you´ll be more concentraded! ";
				}
				else{
				return "You seem to be feeling a little down :worried: , maybe try something easier, like these items:   " ;
				}
			case PS:  
				return "You seem exited :star_struck: , challenge yourself with the following items:   " +valence + "\r\n"+ text; 
			case ANGRY: 
			if (valence < -0.9)
				{
					return "You seem to be feeling angry :face_with_symbols_over_mouth: , consider coming back once you´re calmer to tackle the next challenge! :muscle: ";
				}
				return "You seem angry :angry: , maybe try something less demanding, like, these items:   "  + "\r\n"+ text;
			case NEUTRAL:
			if (valence > 0)
			{
				return "You seem to be feeling calmed :person_getting_massage: , take advantage of it, here are some items that might be more complicated "+ "\r\n" + text;
			}
			return "You seem calmed :person_getting_massage: , here are some items you should work on  "  + "\r\n"+ text;
			default:
				return "ERROR: Current emotion not found in user";
			}
	}

	public static String emotionPast(Emotion emotion, double valence, String text){
		switch (emotion) {
			case UNDEFINED:
				return "You had lower scores in these items: "  + "\r\n"+ text+ "\r\n try and review these topics!";
			case HAPPY:
				if (valence > 0.9)
				{
					return "You seem to be feeling it! :sunglasses: , here is a challenge for you! Try reaproaching these Topics where you **struggled** in the beginning   " + "\r\n"+ text;
				}
				return "You seem to be feeling happy :smile:, take advantage of it, reconsider these Topics where you where **less satisfied** :   "  + "\r\n"+ text;
			case SAD:
				if (valence < -0.9)
				{
					return "You seem to be feeling sad :confused: , consider taking a short break :person_in_lotus_position: and comming back, you´ll be more concentraded! ";
				}
				else{
				return "You seem to be feeling a little down :worried: consider working on these topics where you were more **satisfied** to build up confidence for the exam! "  + "\r\n"+ text;
				}
			case PS:  
				return "You seem exited :star_struck: , challenge yourself with the following items where you where **less satisfied** :   "  + "\r\n"+ text; 
			case ANGRY: 
			if (valence < -0.9)
				{
					return "You seem to be feeling angry :face_with_symbols_over_mouth: , consider coming back once you´re calmer to tackle the next challenge! :muscle: ";
				}
				return "You seem angry :angry: , try and work again on these topics where you were **more confident** to settle them in    "  + "\r\n"+ text;
			case NEUTRAL:
			if (valence > 0)
			{
				return "You seem to be feeling calmed :person_getting_massage: , take advantage of it, here are some items where you **struggled** in the past "+ "\r\n" + text;
			}
			return "You seem calmed :person_getting_massage: , continue like this, heare are some items for you to revise  "  + "\r\n"+ text;
			default:
				return "ERROR: Current emotion not found in user";
			}
	}
}
