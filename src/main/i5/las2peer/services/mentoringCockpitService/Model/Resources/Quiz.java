package i5.las2peer.services.mentoringCockpitService.Model.Resources;

import java.util.HashMap;

public class Quiz extends CompletableResource {
	private HashMap<String, Double> grades;
	private int maxGrade;
	
	@Override
	public String getSuggestionText() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getSuggestionItemText() {
		// TODO Auto-generated method stub
		return null;
	}

	public Quiz(String id, String name, String url, int maxGrade) {
		super(id, name, url);
		this.grades = new HashMap<String, Double>();
		this.maxGrade = maxGrade;
	}
}
