package i5.las2peer.services.mentoringCockpitService.Suggestion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import i5.las2peer.services.mentoringCockpitService.Model.Resources.Resource;

public class SuggestionQueue {
	private HashMap<String, Suggestion> suggestionMap; // Set used for keeping track of all resources that can be suggested
	private HashSet<Suggestion> suggestionSet; // Contains the same items from the suggestion queue but in a set, which facilitates lookup
	private ArrayList<Suggestion> suggestionList; // Queue used for keeping track of what resources should be suggested next
	
	public SuggestionQueue() {
		this.suggestionMap = new HashMap<String, Suggestion>();
		this.suggestionSet = new HashSet<Suggestion>();
		this.suggestionList = new ArrayList<Suggestion>();
	}
	
	public Suggestion getSuggestion() {
		// Return the first resource in the queue that has not been removed from the suggestion set
		Suggestion suggestion = null;
		
		if (!suggestionList.isEmpty()) {
			sort();
			Suggestion temp = null;
			do {
				temp = suggestionList.get(suggestionList.size() - 1);
				suggestionList.remove(suggestionList.size() - 1);			
			} while (!suggestionSet.contains(temp) && !suggestionList.isEmpty());
			if (suggestionSet.contains(temp) ) {
				suggestion = temp;
			}
		}
		if (!suggestionSet.isEmpty() && suggestionList.isEmpty() && suggestion == null) {
			suggestionList = new ArrayList<Suggestion>(suggestionSet);
			suggestion = getSuggestion();
		}
		
		return suggestion;
	}
	
	public void addSuggestion(Suggestion suggestion) {
		Resource resource = suggestion.getSuggestedResource();
		if (suggestionMap.containsKey(resource.getId())) {
			dropSuggestion(resource);
		} 
		suggestionSet.add(suggestion);
		suggestionMap.put(resource.getId(), suggestion);
		suggestionList.add(suggestion);
	}
	
	public void dropSuggestion(Resource resource) {
		suggestionSet.remove(suggestionMap.get(resource.getId()));
		suggestionMap.remove(resource.getId());
	}
	
	public void sort() {
		Comparator<Suggestion> comp = new Comparator<Suggestion>() {
			@Override
			public int compare(Suggestion lhs, Suggestion rhs) {
				return (lhs.getPriority() > rhs.getPriority()) ? 1 : lhs.getPriority() < rhs.getPriority() ? -1 : 0; 
			}
		};
		Collections.sort(suggestionList, comp);
	}
}
