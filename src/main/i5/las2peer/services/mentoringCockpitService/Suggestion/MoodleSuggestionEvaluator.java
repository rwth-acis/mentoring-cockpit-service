package i5.las2peer.services.mentoringCockpitService.Suggestion;

import i5.las2peer.services.mentoringCockpitService.Interactions.Completed;
import i5.las2peer.services.mentoringCockpitService.Interactions.UserResourceInteraction;
import i5.las2peer.services.mentoringCockpitService.Model.User;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.CompletableResource;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.Resource;

public class MoodleSuggestionEvaluator extends SuggestionEvaluator {

	public MoodleSuggestionEvaluator(double minimalPriority, double maximalPriority) {
		super(minimalPriority, maximalPriority);
	}

	@Override
	public double getSuggestionPriority(User user, Resource resource, SuggestionReason reason) {
		switch (reason) {
		case NOT_VIEWED:
			return 0.9;
		case NOT_COMPLETED:
			return 1.0;
		case NOT_MAX_GRADE:
			return 1.0 - getMaxGrade(user, (CompletableResource) resource);
		default:
			return 0;
		}
	}
	
	@Override
	public SuggestionReason getSuggestionReason(User user, Resource resource) {
		if (resource instanceof CompletableResource) {
			CompletableResource completable = (CompletableResource) resource;
			if (!containsInteraction(user.getInteractionLists().get(resource.getId()), "completed")) {
				return SuggestionReason.NOT_COMPLETED;
			} else {
				double maxGrade = getMaxGrade(user, completable);
				if (maxGrade < 1) {
					return SuggestionReason.NOT_MAX_GRADE;
				} else {
					return SuggestionReason.NOT_SUGGESTED;
				}
			}
		} else {
			if (!containsInteraction(user.getInteractionLists().get(resource.getId()), "viewed")) {
				return SuggestionReason.NOT_VIEWED;
			} else {
				return SuggestionReason.NOT_SUGGESTED;
			}
		}
	}
	
	public double getMaxGrade(User user, CompletableResource completable) {
		double res = 0;
		for (UserResourceInteraction interaction : user.getInteractionLists().get(completable.getId())) {
			if (interaction instanceof Completed) {
				Completed completed = (Completed) interaction;
				if (completed.getGrade() > res) {
					res = completed.getGrade();
				}
			}
		}
		return res;
	}
}
