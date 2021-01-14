package i5.las2peer.services.mentoringCockpitService.Model.Resources;

import i5.las2peer.services.mentoringCockpitService.Suggestion.SuggestionReason;
import i5.las2peer.services.mentoringCockpitService.Suggestion.TextFormatter;

public class File extends Resource {
	
	public File(String id, String name, String url) {
		super(id, name, url);
	}

	@Override
	public String getSuggestionText(SuggestionReason reason) {
		switch (reason) {
		case NOT_VIEWED:
			return "You still haven't accessed the file " + name;
		default:
			return "Error";
		}
	}

	@Override
	public String getSuggestionItemText() {
		return "File " + TextFormatter.createHyperlink(name, url);
	}

}
