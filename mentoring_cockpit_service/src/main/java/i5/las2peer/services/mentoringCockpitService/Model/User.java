package i5.las2peer.services.mentoringCockpitService.Model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import i5.las2peer.services.mentoringCockpitService.Model.Resources.Resource;
import i5.las2peer.services.mentoringCockpitService.Suggestion.Emotion;
import i5.las2peer.services.mentoringCockpitService.Suggestion.Suggestion;
import i5.las2peer.services.mentoringCockpitService.Suggestion.SuggestionEvaluator;
import i5.las2peer.services.mentoringCockpitService.Suggestion.SuggestionQueue;
import i5.las2peer.services.mentoringCockpitService.Suggestion.SuggestionReason;


public class User {
	protected String userid;
	protected String name;
	protected ArrayList<Resource> updateSet; // Resources with which the user has interacted since the last data update
	protected SuggestionEvaluator suggestEvaluator;
	protected SuggestionQueue suggestionQueue;
	// last current emotion reading for the user, or null for non-emotional context
	protected double valence; 
	protected Emotion emotion; 
	protected Collection<Resource> firstResources;
	
	public User(String userid, String name, Collection<Resource> resources, SuggestionEvaluator evaluator) {
		this.userid = userid;
		this.name = name;
		this.suggestEvaluator = evaluator;
		this.suggestionQueue = new SuggestionQueue();
		this.updateSet = new ArrayList<Resource>();
		this.firstResources = resources;

		//default value for valence, when undefined.
		this.valence = -100;
		this.emotion = Emotion.UNDEFINED;
	}
	
	public ArrayList<Suggestion> getSuggestion(int numOfSuggestions) {	
		return suggestionQueue.getSuggestion(numOfSuggestions);	
	}
	public void updateValence(double valence)
	{
		this.valence = valence; 
	}
	public void updateEmotion(Emotion emotion)
	{
		this.emotion = emotion; 
	}

	public Emotion getEmotion()
	{
		return this.emotion; 
	}

	public double getValence(){
		return this.valence; 
	}
	
	public void updateSuggestions(Collection<Resource> resources) {
		System.out.println("-DEBUG: Updating suggestions for user:"+ userid);

		//todo: create a function which gets the last emotion reading from the user, from the mognodb
		//This resources are the <<newResources>>
		HashSet<Resource> updates = new HashSet<Resource>(resources);
		if(!this.firstResources.isEmpty()){
			System.out.println("DEBUGFINAL: There are fist resources! ");
			HashSet<Resource> firstUpdates = new HashSet<Resource>(this.firstResources);
			updates.addAll(firstUpdates);
			 //this.firstResources.clear(); These are not clear given we need to reprocess the priority in further iterations
		}

		updates.addAll(updateSet);
		updateSet.clear();
		for (Resource resource : updates) {
			//DEBUG: System.out.println("(!!): Going through resource" +resource.getName());
			SuggestionReason reason = suggestEvaluator.getSuggestionReason(this, resource);
			//DEBUG: System.out.println("--DEBUG: Suggestion evaluator reason: " + reason);
			if (reason != SuggestionReason.NOT_SUGGESTED) {
				//DEBUG: System.out.println("(!!): Adding resource to the prio queue");
				double priority = suggestEvaluator.getSuggestionPriority(this, resource, reason);
				//DEBUG: System.out.println("--DEBUG: Priority given: "+ priority);
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
