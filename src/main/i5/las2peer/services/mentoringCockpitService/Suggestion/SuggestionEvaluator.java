package i5.las2peer.services.mentoringCockpitService.Suggestion;

import i5.las2peer.services.mentoringCockpitService.Model.User;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.Resource;

public abstract class SuggestionEvaluator {
	double minimalPriority;
	double maximalPriority;
	
	public SuggestionEvaluator(double minimalPriority, double maximalPriority) {
		this.minimalPriority = minimalPriority;
		this.maximalPriority = maximalPriority;
	}
	
	public abstract double getSuggestionPriority(User user, Resource resource, SuggestionReason reason);
	
	public abstract SuggestionReason getSuggestionReason(User user, Resource resource);
	
	public abstract boolean hasInteraction(User user, Resource resource, String interactionName);
}
