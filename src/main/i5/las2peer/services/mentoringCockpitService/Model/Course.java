package i5.las2peer.services.mentoringCockpitService.Model;

import i5.las2peer.services.mentoringCockpitService.MentoringCockpitService;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.Resource;
import java.util.HashMap;

public abstract class Course {
	protected String courseid;
	protected String courseURL;
	protected HashMap<String, User> users;
	protected HashMap<String, Resource> resources;
	protected HashMap<String, Theme> themes;
	protected MentoringCockpitService service;
	
	public Course(String courseid, String courseURL, MentoringCockpitService service) {
		this.courseid = courseid;
		this.courseURL = courseURL;
		this.users = new HashMap<String, User>();
		this.resources = new HashMap<String, Resource>();
		this.themes = new HashMap<String, Theme>();
		this.service = service;
	}
	
	public abstract void createUsers();
	
	public abstract void createResources();
	
	public abstract void createThemes();

	public String getCourseid() {
		return courseid;
	}

	public void setCourseid(String courseid) {
		this.courseid = courseid;
	}

	public String getCourseURL() {
		return courseURL;
	}

	public void setCourseURL(String courseURL) {
		this.courseURL = courseURL;
	}

	public HashMap<String, User> getUsers() {
		return users;
	}

	public void setUsers(HashMap<String, User> users) {
		this.users = users;
	}

	public HashMap<String, Resource> getResources() {
		return resources;
	}

	public void setResources(HashMap<String, Resource> resources) {
		this.resources = resources;
	}

	public HashMap<String, Theme> getThemes() {
		return themes;
	}

	public void setThemes(HashMap<String, Theme> themes) {
		this.themes = themes;
	}

	public MentoringCockpitService getService() {
		return service;
	}

	public void setService(MentoringCockpitService service) {
		this.service = service;
	}

}
