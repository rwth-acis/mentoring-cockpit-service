package i5.las2peer.services.mentoringCockpitService.Model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import i5.las2peer.services.mentoringCockpitService.Interactions.UserResourceInteraction;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.Resource;
import i5.las2peer.services.mentoringCockpitService.Suggestion.MoodleSuggestionEvaluator;
import i5.las2peer.services.mentoringCockpitService.Suggestion.Suggestion;
import i5.las2peer.services.mentoringCockpitService.Suggestion.SuggestionEvaluator;
import i5.las2peer.services.mentoringCockpitService.Suggestion.SuggestionQueue;
import i5.las2peer.services.mentoringCockpitService.Suggestion.SuggestionReason;

public class User {
	protected String userid;
	protected String name;
	protected HashMap<String, ArrayList<UserResourceInteraction>> interactionLists; // Map of lists used to keep track of all interactions
	protected ArrayList<Resource> updateSet; // Resources with which the user has interacted since the last data update
	protected SuggestionEvaluator suggestionEvaluator;
	protected SuggestionQueue suggestionQueue;
	
	public User(String userid, String name, Collection<Resource> resources) {
		this.userid = userid;
		this.name = name;
		this.interactionLists = new HashMap<String, ArrayList<UserResourceInteraction>>();
		this.suggestionEvaluator = new MoodleSuggestionEvaluator(0, 1);
		this.suggestionQueue = new SuggestionQueue();
		this.updateSet = new ArrayList<Resource>();
		updateSuggestions(resources);
	}
	
	public ArrayList<Suggestion> getSuggestion(int numOfSuggestions) {	
		return suggestionQueue.getSuggestion(numOfSuggestions);	
	}
	
	public void updateSuggestions(Collection<Resource> resources) {
		HashSet<Resource> updates = new HashSet<Resource>(resources);
		updates.addAll(updateSet);
		updateSet.clear();
		for (Resource resource : updates) {
			SuggestionReason reason = suggestionEvaluator.getSuggestionReason(this, resource);
			if (reason != SuggestionReason.NOT_SUGGESTED) {
				double priority = suggestionEvaluator.getSuggestionPriority(this, resource, reason);
				Suggestion suggestion = new Suggestion(resource, priority, reason);
				suggestionQueue.addSuggestion(suggestion);
			} else {
				suggestionQueue.dropSuggestion(resource);
			}
		}
	}
	
	public String getUserid() {
		return userid;
	}

	public HashMap<String, ArrayList<UserResourceInteraction>> getInteractionLists() {
		return interactionLists;
	}

	public String getName() {
		return name;
	}

	public ArrayList<Resource> getUpdateSet() {
		return updateSet;
	}
	
	
}
