package i5.las2peer.services.mentoringCockpitService.Links;

import i5.las2peer.services.mentoringCockpitService.Model.User;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.Resource;

public abstract class UserResourceLink extends Link {
	protected User user;
	protected Resource resource;
	
	public UserResourceLink(long timestamp, User user, Resource resource) {
		super(timestamp);
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
