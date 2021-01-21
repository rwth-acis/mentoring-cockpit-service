package i5.las2peer.services.mentoringCockpitService.Model;

import i5.las2peer.services.mentoringCockpitService.MentoringCockpitService;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.Resource;
import i5.las2peer.services.mentoringCockpitService.Suggestion.SuggestionEvaluator;
import i5.las2peer.services.mentoringCockpitService.Themes.Theme;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimeZone;

public abstract class Course {
	protected String courseid;
	protected String courseURL;
	protected HashMap<String, User> users;
	protected HashMap<String, Resource> resources;
	protected HashMap<String, Theme> themes;
	protected MentoringCockpitService service;
	protected SuggestionEvaluator suggestionEvaluator;
	protected long lastUpdated = 0;
	protected ArrayList<Resource> newResources;
	
	public Course(String courseid, String courseURL, MentoringCockpitService service, SuggestionEvaluator suggestionEvaluator) {
		this.courseid = courseid;
		this.courseURL = courseURL;
		this.users = new HashMap<String, User>();
		this.resources = new HashMap<String, Resource>();
		this.themes = new HashMap<String, Theme>();
		this.service = service;
		this.lastUpdated = 0;
		this.newResources = new ArrayList<Resource>();
		this.suggestionEvaluator = suggestionEvaluator;
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
	
	public abstract void createInteractions(long since);
	
	public abstract String getSuggestion(String userid);

	public abstract String getThemeSuggestions(String themeid);
	
	public String getCourseid() {
		return courseid;
	}

	public String getCourseURL() {
		return courseURL;
	}

	public HashMap<String, User> getUsers() {
		return users;
	}

	public HashMap<String, Resource> getResources() {
		return resources;
	}

	public HashMap<String, Theme> getThemes() {
		return themes;
	}

	public MentoringCockpitService getService() {
		return service;
	}

	public ArrayList<Resource> getNewResources() {
		return newResources;
	}

}
