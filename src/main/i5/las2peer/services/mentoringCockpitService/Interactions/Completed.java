package i5.las2peer.services.mentoringCockpitService.Interactions;

import i5.las2peer.services.mentoringCockpitService.Model.User;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.CompletableResource;

public class Completed extends UserResourceInteraction {
	private double grade;
	
	public Completed(long timestamp, User user, CompletableResource resource, double grade) {
		super(timestamp, "completed", user, resource);
		this.grade = grade;
	}

	public double getGrade() {
		return grade;
	}

	public void setGrade(double grade) {
		this.grade = grade;
	}
	
}
