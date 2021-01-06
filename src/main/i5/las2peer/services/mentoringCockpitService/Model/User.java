package i5.las2peer.services.mentoringCockpitService.Model;

public abstract class User {
	protected String email;
	protected String name;
	protected Course course;
	
	public User(String email, String name, Course course) {
		super();
		this.email = email;
		this.name = name;
		this.course = course;
	}

	public String getEmail() {
		return email;
	}

	public String getName() {
		return name;
	}

	public Course getCourse() {
		return course;
	}
	
	
}
