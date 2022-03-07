package i5.las2peer.services.mentoringCockpitService.Suggestion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

import i5.las2peer.services.mentoringCockpitService.Model.Resources.Resource;

public class SuggestionQueue {
	private HashMap<String, Suggestion> suggestionMap; // Set used for keeping track of all resources that can be suggested
	private LinkedHashSet<Suggestion> suggestionSet; // Contains the same items from the suggestion queue but in a set, which facilitates lookup
	private ArrayList<Suggestion> suggestionList; // Queue used for keeping track of what resources should be suggested next
	
	public SuggestionQueue() {
		this.suggestionMap = new HashMap<String, Suggestion>();
		this.suggestionSet = new LinkedHashSet<Suggestion>();
		this.suggestionList = new ArrayList<Suggestion>();
	}
	
	public ArrayList<Suggestion> getSuggestion(int number) {
		// Return the first resource in the queue that has not been removed from the suggestion set
		//System.out.println("DEBUG --- SIZE: " + suggestionList.size());
		//System.out.println("DEBUG --- LIST: " + suggestionList);
		ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
		if (!suggestionSet.isEmpty() && number < suggestionSet.size()) {
			sort();
			while (number > 0) {
				if (suggestionList.isEmpty()) {
					suggestionList = new ArrayList<Suggestion>(suggestionSet);
					sort();
				}
				Suggestion temp = suggestionList.get(suggestionList.size() - 1);
				suggestionList.remove(suggestionList.size() - 1);
				if (suggestionSet.contains(temp)) {
					suggestions.add(temp);
					number = number - 1;
				}		
			}
		} else if (number >= suggestionSet.size()) {
			suggestions = new ArrayList<Suggestion>(suggestionSet);
		}
		
		return suggestions;
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
	
	private void sort() {
		Comparator<Suggestion> comp = new Comparator<Suggestion>() {
			@Override
			public int compare(Suggestion lhs, Suggestion rhs) {
				return (lhs.getPriority() > rhs.getPriority()) ? 1 : lhs.getPriority() < rhs.getPriority() ? -1 : 0; 
			}
		};
		Collections.sort(suggestionList, comp);
	}
}
