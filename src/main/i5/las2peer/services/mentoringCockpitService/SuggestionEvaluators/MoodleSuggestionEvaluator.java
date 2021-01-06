package i5.las2peer.services.mentoringCockpitService.SuggestionEvaluators;

import i5.las2peer.services.mentoringCockpitService.Model.MoodleUser;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.Quiz;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.Resource;

public class MoodleSuggestionEvaluator extends SuggestionEvaluator {

	@Override
	public int getPriority(MoodleUser user, Resource resource) {
		if (resource instanceof Quiz) {
			if (!user.getCompletedResources().containsKey(resource.getId())) {
				return 10;
			} else {
				return 10 - (int)(10 * user.getCompletedResources().get(resource.getId()).getGrade());
			}
		} else {
			return 0;
		}
	}

	@Override
	public boolean shouldBeSuggested(MoodleUser user, Resource resource) {
		return (getPriority(user, resource) != 0);
	}

}
