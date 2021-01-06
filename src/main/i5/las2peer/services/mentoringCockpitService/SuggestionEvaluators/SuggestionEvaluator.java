package i5.las2peer.services.mentoringCockpitService.SuggestionEvaluators;

import i5.las2peer.services.mentoringCockpitService.Model.MoodleUser;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.Resource;

public abstract class SuggestionEvaluator {
	
	public abstract int getPriority(MoodleUser user, Resource resource);
	
	public abstract boolean shouldBeSuggested(MoodleUser user, Resource resource);
}
