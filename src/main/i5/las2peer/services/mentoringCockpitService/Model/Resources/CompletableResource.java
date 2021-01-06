package i5.las2peer.services.mentoringCockpitService.Model.Resources;

import java.util.HashMap;

import i5.las2peer.services.mentoringCockpitService.Links.Completed;

public abstract class CompletableResource extends Resource {
	protected HashMap<String, Completed> users;

	public CompletableResource(String id, String name, String url) {
		super(id, name, url);
		this.users = new HashMap<String, Completed>();
	}
	
	public HashMap<String, Completed> getUsers() {
		return users;
	}

	public void setUsers(HashMap<String, Completed> users) {
		this.users = users;
	}
}
