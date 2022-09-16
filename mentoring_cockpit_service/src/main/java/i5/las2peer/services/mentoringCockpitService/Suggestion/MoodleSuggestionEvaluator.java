package i5.las2peer.services.mentoringCockpitService.Suggestion;

import java.util.ArrayList;

import i5.las2peer.services.mentoringCockpitService.Model.User;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.CompletableResource;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.Resource;
import i5.las2peer.services.mentoringCockpitService.SPARQLConnection.SPARQLConnection;

public class MoodleSuggestionEvaluator extends SuggestionEvaluator {
	
	public MoodleSuggestionEvaluator(double minimalPriority, double maximalPriority) {
		super(minimalPriority, maximalPriority);
	}

	@Override
	public double getSuggestionPriority(User user, Resource resource, SuggestionReason reason) {
		switch (reason) {
		case NOT_VIEWED:
			return 0.5;
		case NOT_COMPLETED:
			return 1.0;
		case NOT_MAX_GRADE:
			return 1.0; //- getMaxGrade(user, (CompletableResource) resource)
		default:
			return 0;
		}
	}
	
	@Override
	public SuggestionReason getSuggestionReason(User user, Resource resource) {
		if (resource instanceof CompletableResource) {
			CompletableResource completable = (CompletableResource) resource;
			if (!hasInteraction(user, resource, "completed")) {
				return SuggestionReason.NOT_COMPLETED;
			} else {
				double maxGrade = getBestGrade(user, completable);
				if (maxGrade < 1) {
					return SuggestionReason.NOT_MAX_GRADE;
				} else {
					return SuggestionReason.NOT_SUGGESTED;
				}
			}
		} else {
			if (!hasInteraction(user, resource, "viewed")) {
				return SuggestionReason.NOT_VIEWED;
			} else {
				return SuggestionReason.NOT_SUGGESTED;
			}
		}
	}
	
	public double getBestGrade(User user, CompletableResource completable) {
		double result = 0;
		try {
			return SPARQLConnection.getInstance().getBestGrade(user.getUserid(), completable.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public boolean hasInteraction(User user, Resource resource, String interactionName) {
		boolean result = false;
		try {
			ArrayList<String> interactions = SPARQLConnection.getInstance().getInteractions(user.getUserid(), resource.getId());
			for (String interaction : interactions) {
				if (interaction.contains(interactionName)) { //TODO: change to equals
					result = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
}
