package i5.las2peer.services.mentoringCockpitService.Model;

import i5.las2peer.services.mentoringCockpitService.MentoringCockpitService;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.Resource;
import i5.las2peer.services.mentoringCockpitService.SPARQLConnection.SPARQLConnection;
import i5.las2peer.services.mentoringCockpitService.Suggestion.SuggestionEvaluator;

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
	protected long lastUpdated = 0;
	protected ArrayList<Resource> newResources;
	
	public Course(String courseid, String courseURL, MentoringCockpitService service, SuggestionEvaluator suggestionEvaluator) {
		this.courseid = courseid;
		this.courseURL = courseURL;
		this.users = new HashMap<String, User>();
		this.resources = new HashMap<String, Resource>();
		this.themes = new HashMap<String, Theme>();
		this.service = service;
		SPARQLConnection.startConnection(service.triplestoreDomain);
		this.newResources = new ArrayList<Resource>();
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		try {
			this.lastUpdated = SPARQLConnection.getInstance().getLatestTime(courseid);
		} catch (Exception e) {
			e.printStackTrace();
		}

		updateOntology(lastUpdated);
		updateProfiles(0);
		setTimeToCurrent();
	}
	
	protected void setTimeToCurrent() {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		Instant instant = timestamp.toInstant();
		lastUpdated = instant.getEpochSecond();
	}
	
	public abstract void update();
	
	protected abstract void updateOntology(long since);
	
	protected abstract void updateProfiles(long since);
	
	protected abstract void createUsers(long since);
	
	protected abstract void createResources(long since);
	
	protected abstract void createThemes(long since);
	
	protected abstract void createInteractions(long since);
	
	public abstract String getSuggestion(String userid, int numOfSuggestions);

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
