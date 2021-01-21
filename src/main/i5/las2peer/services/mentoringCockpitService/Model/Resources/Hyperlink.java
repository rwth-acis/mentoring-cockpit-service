package i5.las2peer.services.mentoringCockpitService.Model.Resources;

import i5.las2peer.services.mentoringCockpitService.Suggestion.SuggestionReason;
import i5.las2peer.services.mentoringCockpitService.Suggestion.TextFormatter;

public class Hyperlink extends Resource {
	
	public Hyperlink(String id, String name, String url) {
		super(id, name, url);
	}

	@Override
	public String getSuggestionText(SuggestionReason reason) {
		switch (reason) {
		case NOT_VIEWED:
			return "You still haven't accessed the link " + TextFormatter.quote(TextFormatter.createHyperlink(name, url));
		default:
			return "Error";
		}
	}

	@Override
	public String getSuggestionItemText() {
		return "Link " + TextFormatter.quote(TextFormatter.createHyperlink(name, url));
	}

}
