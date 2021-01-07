package i5.las2peer.services.mentoringCockpitService.Model.Resources;

import i5.las2peer.services.mentoringCockpitService.Suggestion.SuggestionReason;

public class Quiz extends CompletableResource {
	
	public Quiz(String id, String name, String url) {
		super(id, name, url, 0, 10, 5);
	}
	
	@Override
	public String getSuggestionText(SuggestionReason reason) {
		switch(reason) {
			case NOT_COMPLETED:
				return "You still haven't completed the quiz " + this.getName();
			case NOT_MAX_GRADE:
				return "You can try improving your grade in the quiz " + this.getName();
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
