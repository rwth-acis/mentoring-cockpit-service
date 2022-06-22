package i5.las2peer.services.mentoringCockpitService.Model.Resources;

import i5.las2peer.services.mentoringCockpitService.Suggestion.SuggestionReason;
import i5.las2peer.services.mentoringCockpitService.Suggestion.TextFormatter;

public class File extends Resource {
	
	public File(String id, String name, String url) {
		super(id, name, url, "File");
	}

	@Override
	public String getSuggestionText(SuggestionReason reason) {
		switch (reason) {
		case NOT_VIEWED:
			return "There is this file :page_facing_up: " + TextFormatter.quote(TextFormatter.createHyperlink(name, url));
		default:
			return "Error";
		}
	}

	@Override
	public String getSuggestionItemText() {
		return "File " + TextFormatter.quote(TextFormatter.createHyperlink(name, url));
	}

}
