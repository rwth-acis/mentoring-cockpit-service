package i5.las2peer.services.mentoringCockpitService.Model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import i5.las2peer.services.mentoringCockpitService.Interactions.UserResourceInteraction;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.Resource;
import i5.las2peer.services.mentoringCockpitService.Suggestion.Suggestion;
import i5.las2peer.services.mentoringCockpitService.Suggestion.SuggestionEvaluator;
import i5.las2peer.services.mentoringCockpitService.Suggestion.SuggestionReason;

public abstract class User {
	protected String email;
	protected String name;
	protected Course course;
	protected HashMap<String, ArrayList<UserResourceInteraction>> interactionLists; // Map of lists used to keep track of all interactions
	protected HashMap<String, Suggestion> suggestionMap; // Set used for keeping track of all resources that can be suggested
	protected ArrayList<Suggestion> suggestionQueue; // Queue used for keeping track of what resources should be suggested next
	protected HashSet<Suggestion> suggestionSet; // Contains the same items from the suggestion queue but in a set, which facilitates lookup
	protected ArrayList<Resource> recentlyInteractedResources; // Resources with which the user has interacted since the last data update
	protected SuggestionEvaluator suggestionEvaluator;
	
	public User(String email, String name, Course course) {
		this.email = email;
		this.name = name;
		this.course = course;
		this.interactionLists = new HashMap<String, ArrayList<UserResourceInteraction>>(); 
		this.suggestionMap = new HashMap<String, Suggestion>(); 
		this.suggestionQueue = new ArrayList<Suggestion>();
		this.suggestionSet = new HashSet<Suggestion>();
		this.recentlyInteractedResources = new ArrayList<Resource>();
		this.suggestionEvaluator = course.suggestionEvaluator;
		this.recentlyInteractedResources = new ArrayList<Resource>(course.getResources().values());
	}
	
	public Suggestion getSuggestion() {
		//System.out.println("DEBUG --- Interactions: " + interactionLists.keySet());
		// Merge list of new resources and newly interacted resources and update suggestions for them
		HashSet<Resource> updateSet = new HashSet<Resource>(course.getNewResources());
		updateSet.addAll(recentlyInteractedResources);
		updateSuggestions(updateSet);
		
		// Return the first resource in the queue that has not been removed from the suggestion map
		Suggestion suggestion = null;
		if (!suggestionQueue.isEmpty()) {
			do {
				suggestion = suggestionQueue.get(suggestionQueue.size() - 1);
				suggestionQueue.remove(suggestionQueue.size() - 1);
				System.out.println("DEBUG --- ID: " + suggestion.getSuggestedResource().getId());
				
				if (suggestionMap.containsKey(suggestion.getSuggestedResource().getId())) {
					return suggestion;
				}
			} while (!suggestionMap.containsKey(suggestion.getSuggestedResource().getId()) && !suggestionQueue.isEmpty());
		}
		return null;	
	}
	
	public void updateSuggestions(HashSet<Resource> resources) {
		for (Resource resource : resources) {
			SuggestionReason reason = suggestionEvaluator.getSuggestionReason(this, resource);
			if (reason != SuggestionReason.NOT_SUGGESTED) {
				double priority = suggestionEvaluator.getSuggestionPriority(this, resource, reason);
				if (suggestionMap.containsKey(resource.getId())) {
					suggestionMap.get(resource.getId()).setReason(reason);
					suggestionMap.get(resource.getId()).setPriority(priority);
					if (!suggestionSet.contains(suggestionMap.get(resource.getId()))) {
						suggestionQueue.add(suggestionMap.get(resource.getId()));
						suggestionSet.add(suggestionMap.get(resource.getId()));
					}
					
				} else {
					Suggestion suggestion = new Suggestion(resource, priority, reason);
					suggestionMap.put(resource.getId(), suggestion);
					suggestionQueue.add(suggestion);
					suggestionSet.add(suggestion);
				}
			} else if (suggestionMap.containsKey(resource.getId())) {
				suggestionMap.remove(resource.getId());
			}
		}
		if (suggestionQueue.isEmpty()) {
			Collection<Suggestion> restartSet = suggestionMap.values();
			suggestionQueue.addAll(new ArrayList<Suggestion>(restartSet));
			suggestionSet.addAll(new HashSet<Suggestion>(restartSet));
		}
		sortSuggestionQueue();
	};
	
	protected void sortSuggestionQueue () {
		Comparator<Suggestion> comp = new Comparator<Suggestion>() {
			@Override
			public int compare(Suggestion lhs, Suggestion rhs) {
				return (lhs.getPriority() > rhs.getPriority()) ? 1 : lhs.getPriority() < rhs.getPriority() ? -1 : 0; 
			}
		};
		Collections.sort(suggestionQueue, comp);
	}

	public String getEmail() {
		return email;
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
