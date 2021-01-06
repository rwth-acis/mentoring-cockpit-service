package i5.las2peer.services.mentoringCockpitService.Model;

import i5.las2peer.services.mentoringCockpitService.Model.Resources.Resource;
import i5.las2peer.services.mentoringCockpitService.Links.Completed;
import i5.las2peer.services.mentoringCockpitService.Links.Viewed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

public class MoodleUser extends User {
	//private HashMap<String, Resource> viewedResources;
	//private HashMap<String, CompletableResource> completedResources;
	//private HashMap<String, Double> grades;
	private HashMap<String, Viewed> viewedResources;
	private HashMap<String, Completed> completedResources;
	private HashSet<Resource> suggestionSet; // Suggestion hash set for keeping the possible suggestions by course
	private ArrayList<Resource> suggestionQueue; // Suggestion queue for the user by course

	public MoodleUser(String email, String name, Course course) {
		super(email, name, course);
		this.viewedResources = new HashMap<String, Viewed>();
		this.completedResources = new HashMap<String, Completed>();
		//this.grades = new HashMap<String, Double>();
		this.suggestionSet = new HashSet<Resource>(); // Set used for keeping track of all resources that can be suggested
		this.suggestionQueue = new ArrayList<Resource>(); // Queue used for keeping track of what resources should be suggested next
	}
	
	public String getSuggestion(String courseid) {
		updateSuggestions();
		Resource resource = null;
		
		// Return the first resource in the queue that has not been removed from the suggestion set
		while (!suggestionSet.contains(resource) && !suggestionQueue.isEmpty()) {
			resource = suggestionQueue.get(0);
			suggestionQueue.remove(0);
			if (suggestionSet.contains(resource)) {
				return resource.getSuggestionText(email);
			}
		}
		return "No suggestion could be found";
	}
	
	public void updateSuggestions() {
		// Check for all course resources if they should be in the suggestion set
		for (Entry<String, Resource> resourceEntry : course.getResources().entrySet()) {
			Resource resource = resourceEntry.getValue();
			
			// If resource should be suggested, add it to queue and set
			if (!suggestionSet.contains(resource) && course.suggestionEvaluator.shouldBeSuggested(this, resource)) {
				suggestionSet.add(resource);
				suggestionQueue.add(resource);
			}
			
			// If resource is in set but should not be suggested anymore, remove it
			if (suggestionSet.contains(resource) && !course.suggestionEvaluator.shouldBeSuggested(this, resource)) {
				suggestionSet.remove(resource);
			}
		}
		if (suggestionQueue.isEmpty()) {
			suggestionQueue.addAll(new ArrayList<>(suggestionSet));
		}
		sortQueue();
	}
	
	public void sortQueue () {
		MoodleUser user = this;
		Comparator<Resource> comp = new Comparator<Resource>() {
			@Override
			public int compare(Resource lhs, Resource rhs) {
				return (course.suggestionEvaluator.getPriority(user, lhs) > course.suggestionEvaluator.getPriority(user, rhs)) ? -1 : course.suggestionEvaluator.getPriority(user, lhs) < course.suggestionEvaluator.getPriority(user, rhs) ? 1 : 0; 
			}
		};
		Collections.sort(this.suggestionQueue, comp);
	}

	public HashMap<String, Viewed> getViewedResources() {
		return viewedResources;
	}

	public HashMap<String, Completed> getCompletedResources() {
		return completedResources;
	}

	public HashSet<Resource> getSuggestionSet() {
		return suggestionSet;
	}

	public ArrayList<Resource> getSuggestionQueue() {
		return suggestionQueue;
	}

	public void setSuggestionQueue(ArrayList<Resource> suggestionQueue) {
		this.suggestionQueue = suggestionQueue;
	}
}
