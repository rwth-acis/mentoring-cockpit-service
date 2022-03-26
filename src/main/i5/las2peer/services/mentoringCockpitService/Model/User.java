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
	//last current emotion reading for the user, or null for non-emotional contexti
	protected double valence; 
	
	public User(String userid, String name, Collection<Resource> resources) {
		this.userid = userid;
		this.name = name;
		//changed to the new suggestion evaluator to test it a little
		this.suggestionEvaluator = new ERecSuggestionEvaluator(0, 1);
		//(!) todo: Once the transmition of data from the LRS -> Sparql -> MCS work fine, we can change the suggestion Evaluator to the new one.
		//this.suggestionEvaluator = new ERecSuggestionEvaluator(0,1);
		this.suggestionQueue = new SuggestionQueue();
		this.updateSet = new ArrayList<Resource>();
		updateSuggestions(resources);
		//default value for valence
		this.valence = -1;
	}
	
	public ArrayList<Suggestion> getSuggestion(int numOfSuggestions) {	
		return suggestionQueue.getSuggestion(numOfSuggestions);	
	}
	public void updateValence(double valence)
	{
		this.valence = valence; 
	}

	public double getValence(){
		return this.valence; 
	}
	
	public void updateSuggestions(Collection<Resource> resources) {
		System.out.println("Updating suggestions for user:"+ userid);
		//update the last current emotion from the mongodb
		//todo: create a function which gets the last emotion reading from the user, from the mognodb
		//This resources are the <<newResources>>
		HashSet<Resource> updates = new HashSet<Resource>(resources);
		//the <<<updateSet>>> in the course.updateProfiles(long since) method, resources come from sparql.getupdates(since, courseid)
		//the <<newResources>> comes from resources which were NOT stored previously in the course class. This is passed on user.updateSuggestions(newResources)
		//both serve the exact same fucntion of storing the resources which should be analysed for possible recommendation

		//both are added to the updates HashSet
		//todo: One of the problems is that both newResources, and updateSet are only updated while creating the course!
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
//Solved the ridiculoud need for a new method by adding the valence a protected variable to each user
	// public void updateSuggestionsEmotion(Collection<Resource> resources, double valence) {
	// 	//update the last current emotion from the mongodb
	// 	HashSet<Resource> updates = new HashSet<Resource>(resources);
	// 	updates.addAll(updateSet);
	// 	updateSet.clear();
	// 	for (Resource resource : updates) {
	// 		SuggestionReason reason = suggestionEvaluator.getSuggestionReason(this, resource);
	// 		if (reason != SuggestionReason.NOT_SUGGESTED) {
	// 			double priority = suggestionEvaluator.getSuggestionPriority(this, resource, reason);
	// 			Suggestion suggestion = new Suggestion(resource, priority, reason);
	// 			suggestionQueue.addSuggestion(suggestion);
	// 		} else {
	// 			suggestionQueue.dropSuggestion(resource);
	// 		}
	// 	}
	// }
	
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
