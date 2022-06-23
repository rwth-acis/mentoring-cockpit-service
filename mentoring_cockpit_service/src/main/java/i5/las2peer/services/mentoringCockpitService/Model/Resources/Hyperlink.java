package i5.las2peer.services.mentoringCockpitService.Model.Resources;

import i5.las2peer.services.mentoringCockpitService.Suggestion.SuggestionReason;
import i5.las2peer.services.mentoringCockpitService.Suggestion.TextFormatter;

public class Hyperlink extends Resource {
	
	public Hyperlink(String id, String name, String url) {
		super(id, name, url, "Hyperlink");
	}

	@Override
	public String getSuggestionText(SuggestionReason reason, boolean html) {
		switch (reason) {
		case NOT_VIEWED:
			if (html) {
				return "You still haven't accessed the link " + TextFormatter.quote(TextFormatter.createHTMLHyperlink(name, url));
			} else {
				return "You still haven't accessed the link " + TextFormatter.quote(TextFormatter.createChatHyperlink(name, url));
			}
		default:
			return "Error";
		}
	}

	@Override
	public String getSuggestionItemText(boolean html) {
		if (html) {
			return "Link " + TextFormatter.quote(TextFormatter.createHTMLHyperlink(name, url));
		} else {
			return "Link " + TextFormatter.quote(TextFormatter.createChatHyperlink(name, url));
		}
	}

}
