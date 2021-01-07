package i5.las2peer.services.mentoringCockpitService.Interactions;

import i5.las2peer.services.mentoringCockpitService.Model.User;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.Resource;

public abstract class UserResourceInteraction extends Interaction {
	protected User user;
	protected Resource resource;
	
	public UserResourceInteraction(long timestamp, String name, User user, Resource resource) {
		super(timestamp, name);
		this.user = user;
		this.resource = resource;
	}

	public User getUser() {
		return user;
	}

	public Resource getResource() {
		return resource;
	}
}
