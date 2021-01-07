package i5.las2peer.services.mentoringCockpitService.Model.Resources;

import i5.las2peer.services.mentoringCockpitService.Suggestion.SuggestionReason;

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
		// TODO Auto-generated method stub
		return null;
	}

}
