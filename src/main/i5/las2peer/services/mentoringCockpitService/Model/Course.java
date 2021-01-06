package i5.las2peer.services.mentoringCockpitService.Model;

import i5.las2peer.services.mentoringCockpitService.MentoringCockpitService;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.Resource;
import i5.las2peer.services.mentoringCockpitService.SuggestionEvaluators.SuggestionEvaluator;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.TimeZone;

public abstract class Course {
	protected String courseid;
	protected String courseURL;
	protected HashMap<String, MoodleUser> users;
	protected HashMap<String, Resource> resources;
	protected HashMap<String, Theme> themes;
	protected MentoringCockpitService service;
	protected SuggestionEvaluator suggestionEvaluator;
	protected long lastUpdated = 0;
	
	public Course(String courseid, String courseURL, MentoringCockpitService service) {
		this.courseid = courseid;
		this.courseURL = courseURL;
		this.users = new HashMap<String, MoodleUser>();
		this.resources = new HashMap<String, Resource>();
		this.themes = new HashMap<String, Theme>();
		this.service = service;
		this.lastUpdated = 0;
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		updateKnowledgeBase(lastUpdated);
	}
	
	protected void setTimeToCurrent() {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		Instant instant = timestamp.toInstant();
		lastUpdated = instant.getEpochSecond();
	}
	
	public abstract void updateKnowledgeBase(long since);
	
	public abstract void createUsers(long since);
	
	public abstract void createResources(long since);
	
	public abstract void createThemes(long since);
	
	public abstract void createLinks(long since);
	
	public abstract String getSuggestion(String email, String courseid);

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

	public HashMap<String, MoodleUser> getUsers() {
		return users;
	}

	public void setUsers(HashMap<String, MoodleUser> users) {
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
