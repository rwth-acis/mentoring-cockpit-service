package i5.las2peer.services.mentoringCockpitService.Suggestion;

import java.util.ArrayList;

import i5.las2peer.services.mentoringCockpitService.Interactions.UserResourceInteraction;
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
	
	public boolean containsInteraction(ArrayList<UserResourceInteraction> interactions, String interactionName) {
		if (interactions != null) {
			for (UserResourceInteraction interaction : interactions) {
				if (interaction.getName().equals(interactionName)) {
					return true;
				}
			}
		}
		return false;
	}
}
