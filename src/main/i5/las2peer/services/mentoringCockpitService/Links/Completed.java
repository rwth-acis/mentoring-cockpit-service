package i5.las2peer.services.mentoringCockpitService.Links;

import i5.las2peer.services.mentoringCockpitService.Model.User;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.CompletableResource;

public class Completed extends UserResourceLink {
	private double grade;
	
	public Completed(long timestamp, User user, CompletableResource resource, double grade) {
		super(timestamp, user, resource);
	}

	public double getGrade() {
		return grade;
	}

	public void setGrade(double grade) {
		this.grade = grade;
	}
	
}
