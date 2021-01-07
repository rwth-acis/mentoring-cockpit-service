package i5.las2peer.services.mentoringCockpitService.Interactions;

import i5.las2peer.services.mentoringCockpitService.Model.User;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.Resource;

public class Viewed extends UserResourceInteraction {

	public Viewed(long timestamp, User user, Resource resource) {
		super(timestamp, "viewed", user, resource);
	}
}
