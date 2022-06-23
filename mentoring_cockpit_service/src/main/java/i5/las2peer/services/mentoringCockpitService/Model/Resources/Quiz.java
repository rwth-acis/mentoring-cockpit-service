package i5.las2peer.services.mentoringCockpitService.Model.Resources;

import i5.las2peer.services.mentoringCockpitService.Suggestion.TextFormatter;

public class Quiz extends CompletableResource {
	
	public Quiz(String id, String name, String url) {
		super(id, name, url, "Quiz", 0, 10, 5);
	}

	@Override
	public String getSuggestionItemText(boolean html) {
		if (html) {
			return  TextFormatter.quote(TextFormatter.createHTMLHyperlink(name, url));
		} else {
			return  TextFormatter.quote(TextFormatter.createChatHyperlink(name, url));
		}
	}
}
