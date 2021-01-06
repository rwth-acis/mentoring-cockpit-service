package i5.las2peer.services.mentoringCockpitService.Model.Resources;

public class Quiz extends CompletableResource {
	
	public Quiz(String id, String name, String url, int maxGrade) {
		super(id, name, url);
	}
	
	@Override
	public String getSuggestionText(String email) {
		if (users.containsKey(email)) {
			return "<p>You can try improving your grade in the quiz " + this.getName() + "</p>";
		} else {
			return "<p>You still haven't completed the quiz " + this.getName() + "</p>";
		}
	}


	@Override
	public String getSuggestionItemText() {
		// TODO Auto-generated method stub
		return null;
	}
}
