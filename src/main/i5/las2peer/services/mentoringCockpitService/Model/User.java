package i5.las2peer.services.mentoringCockpitService.Model;

import i5.las2peer.services.mentoringCockpitService.Model.Resources.Resource;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.CompletableResource;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.Quiz;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.LectureSlides;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

public class User {
	private String email;
	private String name;
	private HashMap<String, Course> courses;
	private HashMap<String, Resource> viewedResources;
	private HashMap<String, CompletableResource> completedResources;
	private HashMap<String, Double> grades;
	private ArrayList<Resource> suggestionQueue;

	public User(String email, String name) {
		this.email = email;
		this.name = name;
		this.courses = new HashMap<String, Course>();
		this.viewedResources = new HashMap<String, Resource>();
		this.completedResources = new HashMap<String, CompletableResource>();
		this.grades = new HashMap<String, Double>();
		this.suggestionQueue = new ArrayList<Resource>();
	}
	
	public int getPriority(Resource resource) {
		if (resource instanceof Quiz) {
			if (!completedResources.containsKey(resource.getId())) {
				return 10;
			} else {
				return 10 - (int)(10 * grades.get(resource.getId()).doubleValue());
			}
		} else {
			return 0;
		}
	}
	
	public void sortQueue () {
		Comparator<Resource> comp = new Comparator<Resource>() {
			@Override
			public int compare(Resource lhs, Resource rhs) {
				return (getPriority(lhs) > getPriority(rhs)) ? -1 : (getPriority(lhs) < getPriority(rhs)) ? 1 : 0; 
			}
		};
		Collections.sort(this.suggestionQueue, comp);
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HashMap<String, Course> getCourses() {
		return courses;
	}

	public void setCourses(HashMap<String, Course> courses) {
		this.courses = courses;
	}

	public HashMap<String, Resource> getViewedResources() {
		return viewedResources;
	}

	public void setViewedResources(HashMap<String, Resource> viewedResources) {
		this.viewedResources = viewedResources;
	}

	public HashMap<String, CompletableResource> getCompletedResources() {
		return completedResources;
	}

	public void setCompletedResources(HashMap<String, CompletableResource> completedResources) {
		this.completedResources = completedResources;
	}

	public HashMap<String, Double> getGrades() {
		return grades;
	}

	public void setGrades(HashMap<String, Double> grades) {
		this.grades = grades;
	}

	public ArrayList<Resource> getSuggestionQueue() {
		return suggestionQueue;
	}

	public void setSuggestionQueue(ArrayList<Resource> suggestionQueue) {
		this.suggestionQueue = suggestionQueue;
	}
	
	
}
