package i5.las2peer.services.mentoringCockpitService.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import i5.las2peer.services.mentoringCockpitService.Interactions.UserResourceInteraction;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.Resource;
import i5.las2peer.services.mentoringCockpitService.Suggestion.Suggestion;
import i5.las2peer.services.mentoringCockpitService.Suggestion.SuggestionEvaluator;
import i5.las2peer.services.mentoringCockpitService.Suggestion.SuggestionQueue;
import i5.las2peer.services.mentoringCockpitService.Suggestion.SuggestionReason;

public abstract class User {
	protected String userid;
	protected String name;
	protected Course course;
	protected HashMap<String, ArrayList<UserResourceInteraction>> interactionLists; // Map of lists used to keep track of all interactions
	protected ArrayList<Resource> recentlyInteractedResources; // Resources with which the user has interacted since the last data update
	protected SuggestionEvaluator suggestionEvaluator;
	protected SuggestionQueue suggestionQueue;
	
	public User(String userid, String name, Course course) {
		this.userid = userid;
		this.name = name;
		this.course = course;
		this.interactionLists = new HashMap<String, ArrayList<UserResourceInteraction>>();
		this.suggestionEvaluator = course.suggestionEvaluator;
		this.recentlyInteractedResources = new ArrayList<Resource>(course.getResources().values());
		this.suggestionQueue = new SuggestionQueue();
	}
	
	public ArrayList<Suggestion> getSuggestion(int numOfSuggestions) {
		//System.out.println("DEBUG --- Interactions: " + interactionLists.keySet());
		// Merge list of new resources and newly interacted resources and update suggestions for them		
		HashSet<Resource> updateSet = new HashSet<Resource>(course.getNewResources());
		updateSet.addAll(recentlyInteractedResources);
		recentlyInteractedResources.clear();
		updateSuggestions(updateSet);
		
		return suggestionQueue.getSuggestion(numOfSuggestions);	
	}
	
	public void updateSuggestions(HashSet<Resource> resources) {
		for (Resource resource : resources) {
			SuggestionReason reason = suggestionEvaluator.getSuggestionReason(this, resource);
			if (reason != SuggestionReason.NOT_SUGGESTED) {
				double priority = suggestionEvaluator.getSuggestionPriority(this, resource, reason);
				Suggestion suggestion = new Suggestion(resource, priority, reason);
				suggestionQueue.addSuggestion(suggestion);
			} else {
				suggestionQueue.dropSuggestion(resource);
			}
		}
	};
	
	public String getUserid() {
		return userid;
	}

	public HashMap<String, ArrayList<UserResourceInteraction>> getInteractionLists() {
		return interactionLists;
	}

	public String getName() {
		return name;
	}

	public Course getCourse() {
		return course;
	}

	public ArrayList<Resource> getRecentlyInteractedResources() {
		return recentlyInteractedResources;
	}
	
	
}
