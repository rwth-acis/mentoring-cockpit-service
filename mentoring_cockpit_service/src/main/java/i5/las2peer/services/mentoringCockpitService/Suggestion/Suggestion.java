package i5.las2peer.services.mentoringCockpitService.Suggestion;

import i5.las2peer.services.mentoringCockpitService.Model.Resources.Resource;

public class Suggestion {
	private Resource suggestedResource;
	private double priority;
	private SuggestionReason reason;
	
	public Suggestion(Resource suggestedResource, double priority, SuggestionReason reason) {
		this.suggestedResource = suggestedResource;
		this.priority = priority;
		this.reason = reason;
	}
	
	public String getSuggestionText() {
		return suggestedResource.getSuggestionText(reason);
	}

	public Resource getSuggestedResource() {
		return suggestedResource;
	}

	public double getPriority() {
		return priority;
	}

	public SuggestionReason getReason() {
		return reason;
	}

	public void setPriority(double priority) {
		this.priority = priority;
	}

	public void setReason(SuggestionReason reason) {
		this.reason = reason;
	}
	
	
}
