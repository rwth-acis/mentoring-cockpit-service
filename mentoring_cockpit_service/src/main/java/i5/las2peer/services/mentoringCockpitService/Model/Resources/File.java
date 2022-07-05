package i5.las2peer.services.mentoringCockpitService.Model.Resources;

import i5.las2peer.services.mentoringCockpitService.Suggestion.SuggestionReason;
import i5.las2peer.services.mentoringCockpitService.Suggestion.TextFormatter;

public class File extends Resource {
	
	public File(String id, String name, String url) {
		super(id, name, url, "File");
	}

	@Override
	public String getSuggestionText(SuggestionReason reason, boolean html) {
		switch (reason) {
		case NOT_VIEWED:
			if (html) {
				return "There is this file " + TextFormatter.quote(TextFormatter.createHTMLHyperlink(name, url));
			} else {
				return "There is this file :page_facing_up: " + TextFormatter.quote(TextFormatter.createChatHyperlink(name, url));
			}
		default:
			return "Error";
		}
	}

	@Override
	public String getSuggestionItemText(boolean html) {
		if (html) {
			return "File " + TextFormatter.quote(TextFormatter.createHTMLHyperlink(name, url));
		} else {
			return "File " + TextFormatter.quote(TextFormatter.createChatHyperlink(name, url));
		}
	}

}
