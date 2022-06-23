package i5.las2peer.services.mentoringCockpitService.Suggestion;

import java.util.ArrayList;

public class TextFormatter {
	
	public static String createChatHyperlink(String text, String url) {
		return "["+text+"]"+"("+url+")";
	}

	public static String createHTMLHyperlink(String text, String url) {
		return "<a href=' " + url + "'>" + text + "</a>";
	}

	public static String createChatList(ArrayList<String> itemlist) {
		String res = "";
		for (String item : itemlist) {
			res = res + "\r\n" + item + "";
		}
		return res + "";
	}

	public static String createHTMLList(ArrayList<String> itemlist) {
		String res = "<ul>";
		for (String item : itemlist) {
			res = res + "<li>" + item + "</li>";
		}
		return res + "</ul>";
	}

	public static String createOrderedHTMLList(ArrayList<String> itemlist) {
		String res = "<ol>";
		for (String item : itemlist) {
			res = res + "<li>" + item + "</li>";
		}
		return res + "</ol>";
	}

	public static String createOrderedChatList(ArrayList<String> itemlist) {
		String res = "";
		int i = 1;
		for (String item : itemlist) {
			res = i + ". " + res + "\r\n" + item + "";
			i++;
		}
		return res + "";
	}
	
	public static String quote(String text) {
		return "\"" + text + "\"";
	}

	public static String emotion(Emotion emotion, double valence, String text){
		switch (emotion) {
			case UNDEFINED:
				return "We were not able to identify your current emotion :sad:, but here are some items for you to work on: ";
			case HAPPY:
				if (valence > 0.9)
				{
					return "You seem to be feeling it! :sunglasses: , here is a challenge for you! Try working on the items which should be a little more **complicated**   "+ "\r\n"+ text;
				}
				return "You seem to be feeling happy :smile:, take advantage of it, challenge yourself try something a little **more complex** and tackle these items:   " + "\r\n"+ text;
			case SAD:
				if (valence < -0.9)
				{
					return "You seem to be feeling sad :confused: , consider taking a short break :person_in_lotus_position: and comming back, you´ll be more concentraded! ";
				}
				else{
				return "You seem to be feeling a little down :worried: , maybe try something **a litle easier**, like these items:   "+ "\r\n" +text ;
				}
			case PS:  
				return "You seem exited :star_struck: , challenge yourself with the following items which should be **more difficult**:   " +"\r\n"+ text; 
			case ANGRY: 
			if (valence < -0.9)
				{
					return "You seem to be feeling angry :face_with_symbols_over_mouth: , consider coming back once you´re calmer to tackle the next challenge! :muscle: ";
				}
				return "You seem angry :angry: , maybe try some **less demanding** items, like the following :   "  + "\r\n"+ text;
			case NEUTRAL:
			if (valence > 0)
			{
				return "You seem to be feeling calmed :person_getting_massage: , take advantage of your mental state , here are some items that might be **more complicated** "+ "\r\n" + text;
			}
			return "You seem calmed :person_getting_massage: , here are some items which should not be so **complicated**. Work on them and start to get in learning rythm "  + "\r\n"+ text;
			default:
				return "ERROR: Current emotion not found in user";
			}
	}

	public static String emotionPast(Emotion emotion, double valence, String text){
		if(text.length()<1){return "You seem "+ emotion+ ":face_with_monocle: "+" but you haven´t responded any Item questionnaire, try one by asking for the Lime Surveys in the chat! :clipboard: ";}
		switch (emotion) {
			case UNDEFINED:
				return "You had lower scores in these items: "  + "\r\n"+ text+ "\r\n try and review these topics!";
			case HAPPY:
				if (valence > 0.9)
				{
					return "You seem to be feeling it! :sunglasses: , here is a challenge for you! Try reaproaching these Topics where you **struggled** in the beginning   " + "\r\n"+ text + "\r\n"+ "Once you feel you feel more confident with them feel free to retake the item questionnaire! :pencil: " ;
				}
				return "You seem to be feeling happy :smile:, take advantage of it, reconsider these Topics where you where **less satisfied** :   "  + "\r\n"+ text+ "\r\n"+ "Once you feel you feel more confident with these topics feel free to retake the item questionnaire! :pencil: " ;
			case SAD:
				if (valence < -0.9)
				{
					return "You seem to be feeling sad :confused: , consider taking a short break :person_in_lotus_position: and comming back, you´ll be more concentraded! ";
				}
				else{
				return "You seem to be feeling a little down :worried: consider working on these topics where you were more **satisfied** to build up confidence for the exam! "  + "\r\n"+ text+ "\r\n"+ "Once you feel more up to it, take on a bigger challenge :nerd: " ;
				}
			case PS:  
				return "You seem exited :star_struck: , challenge yourself with the following items where you where **less satisfied** :   "  + "\r\n"+ text+ "\r\n"+ "Once you feel you feel more confident with these topics feel free to retake the item questionnaire! :pencil: " ;
			case ANGRY: 
			if (valence < -0.9)
				{
					return "You seem to be feeling angry :face_with_symbols_over_mouth: , consider coming back once you´re calmer to tackle the next challenge! :muscle: You´ll take better advantage of your time ";
				}
				return "You seem angry :angry: , try and work again on these topics where you were **more confident** to settle them in    "  + "\r\n"+ text;
			case NEUTRAL:
			if (valence > 0)
			{
				return "You seem to be feeling calmed :person_getting_massage: , take advantage of it, here are some items where you **struggled** in the past "+ "\r\n" + text+ "\r\n"+ "Once you feel you feel more confident with these topics feel free to retake the item questionnaire! :pencil: " ;
			}
			return "You seem calmed :person_getting_massage: , continue like this, here are some items for you to revise  "  + "\r\n"+ text;
			default:
				return "ERROR: Current emotion not found in user";
			}
	}
}
