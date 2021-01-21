package i5.las2peer.services.mentoringCockpitService.Model.Resources;

import i5.las2peer.services.mentoringCockpitService.Suggestion.SuggestionReason;
import i5.las2peer.services.mentoringCockpitService.Suggestion.TextFormatter;

public class Quiz extends CompletableResource {
	
	public Quiz(String id, String name, String url) {
		super(id, name, url, 0, 10, 5);
	}
	
	@Override
	public String getSuggestionText(SuggestionReason reason) {
		switch(reason) {
			case NOT_COMPLETED:
				return "You still haven't completed the quiz " + TextFormatter.quote(TextFormatter.createHyperlink(name, url));
			case NOT_MAX_GRADE:
				return "You can try improving your grade in the quiz " + TextFormatter.createHyperlink(name, url);
			default:
				return "Error";
		}
	}

	@Override
	public String getSuggestionItemText() {
		return "Quiz " + TextFormatter.quote(TextFormatter.createHyperlink(name, url));
	}
}
