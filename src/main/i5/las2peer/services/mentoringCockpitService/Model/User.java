package i5.las2peer.services.mentoringCockpitService.Model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import i5.las2peer.services.mentoringCockpitService.Model.Resources.Resource;
import i5.las2peer.services.mentoringCockpitService.Suggestion.ERecSuggestionEvaluator;
import i5.las2peer.services.mentoringCockpitService.Suggestion.MoodleSuggestionEvaluator;
import i5.las2peer.services.mentoringCockpitService.Suggestion.Suggestion;
import i5.las2peer.services.mentoringCockpitService.Suggestion.SuggestionEvaluator;
import i5.las2peer.services.mentoringCockpitService.Suggestion.SuggestionQueue;
import i5.las2peer.services.mentoringCockpitService.Suggestion.SuggestionReason;

public class User {
	protected String userid;
	protected String name;
	protected ArrayList<Resource> updateSet; // Resources with which the user has interacted since the last data update
	protected SuggestionEvaluator suggestionEvaluator;
	protected SuggestionQueue suggestionQueue;
	//list where the last current emotion is saved
	protected ArrayList<Double> valence; 
	
	public User(String userid, String name, Collection<Resource> resources) {
		this.userid = userid;
		this.name = name;
		this.suggestionEvaluator = new MoodleSuggestionEvaluator(0, 1);
		//(!) todo: Once the transmition of data from the LRS -> Sparql -> MCS work fine, we can change the suggestion Evaluator to the new one.
		//this.suggestionEvaluator = new ERecSuggestionEvaluator(0,1);
		this.suggestionQueue = new SuggestionQueue();
		this.updateSet = new ArrayList<Resource>();
		updateSuggestions(resources);
	}
	
	public ArrayList<Suggestion> getSuggestion(int numOfSuggestions) {	
		return suggestionQueue.getSuggestion(numOfSuggestions);	
	}
	
	public void updateSuggestions(Collection<Resource> resources) {
		System.out.println("Updating suggestions for user:"+ userid);
		//update the last current emotion from the mongodb
		//todo: create a function which gets the last emotion reading from the user, from the mognodb
		//this does not work either
		HashSet<Resource> updates = new HashSet<Resource>(resources);
		updates.addAll(updateSet);
		updateSet.clear();
		for (Resource resource : updates) {
			System.out.println("(!!): Going through resource" +resource.getName());
			SuggestionReason reason = suggestionEvaluator.getSuggestionReason(this, resource);
			if (reason != SuggestionReason.NOT_SUGGESTED) {
				System.out.println("(!!): Adding resource to the prio queue");
				double priority = suggestionEvaluator.getSuggestionPriority(this, resource, reason);
				Suggestion suggestion = new Suggestion(resource, priority, reason);
				suggestionQueue.addSuggestion(suggestion);
			} else {
				suggestionQueue.dropSuggestion(resource);
			}
		}
	}

	public void updateSuggestionsEmotion(Collection<Resource> resources, double valence) {
		//update the last current emotion from the mongodb
		//todo: create a function which gets the last emotion reading from the user, from the mognodb
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

	public String getName() {
		return name;
	}

	public ArrayList<Resource> getUpdateSet() {
		return updateSet;
	}
	
	
}
