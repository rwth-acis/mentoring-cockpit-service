package i5.las2peer.services.mentoringCockpitService.Suggestion;

import java.util.ArrayList;

import javax.print.attribute.PrintJobAttribute;

import i5.las2peer.services.mentoringCockpitService.Model.User;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.CompletableResource;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.Resource;
import i5.las2peer.services.mentoringCockpitService.SPARQLConnection.SPARQLConnection;

public class ERecSuggestionEvaluator extends SuggestionEvaluator {
	
	public ERecSuggestionEvaluator(double minimalPriority, double maximalPriority) {
		super(minimalPriority, maximalPriority);
	}

	@Override
	public double getSuggestionPriority(User user, Resource resource, SuggestionReason reason) {
		double cognitiveLoadTemp = getCognitiveLoad(resource);
		if (user.getValence() != -100/*which is the default value when valence is not initialized*/ && cognitiveLoadTemp != 0){
			System.out.println("--DEBUG: Attempting to get priority with emotion");
			double valence = user.getValence(); 
			double cognitiveLoad = getCognitiveLoad(resource);
			double priority = 0; 
			double base = 1; 


			switch(reason){
	
				case NOT_VIEWED: 
					//Here priority is normalize into (0.1) (!)todo: replace 5, and 0 to min and max depending on the values of emotion and cognitive load
					System.out.println("--DEBUG: Resource was not seen, valence : "+valence+" cognitiveLoad: "+cognitiveLoad);
					if (valence > 0){
						priority = base*cognitiveLoad;
					}
					else{
						priority = base/cognitiveLoad; 
					}
					//priority = ((1-(valence-cognitiveLoad))-5)/5;
					return priority;
				case NOT_COMPLETED: 
					System.out.println("--DEBUG: Resource was not completed, valence : "+valence+" cognitiveLoad: "+cognitiveLoad);
					//NOT_COMPLETED also covers quizes for which the maximum grade has not been achieved, not so clar

					// priority = ((1-(valence-cognitiveLoad))-5)/5-0.3;
					if (valence > 0){
						priority = base*cognitiveLoad;
					}
					else{
						priority = base/cognitiveLoad; 
					}
					return (priority); 

				case NOT_MAX_GRADE: 
					System.out.println("--DEBUG: Max grade was not achieved, valence : "+valence+" cognitiveLoad: "+cognitiveLoad);

					if (valence > 0){
						priority = base*cognitiveLoad;
					}
					else{
						priority = base/cognitiveLoad; 
					}
					//priority = ((1-(valence-cognitiveLoad))-5)/5;
					return (priority-0.3);


	
					//Since we are concentrated in unseen items, every item that has been interacted with already receieves a priority of 0.
				default: 
					return 0 ;  	
	
			}
		}
		else{
			System.out.println("--DEUBG: Defaulting to standard suggestions");

			//default suggestion architecture

			switch (reason) {
				case NOT_VIEWED:
					return 0.5;
				case NOT_COMPLETED:
					return 1.0;
				case NOT_MAX_GRADE:
					return 1.0; //- getMaxGrade(user, (CompletableResource) resource)
				default:
					return 0;
				}

		}


	}


	// public double getSuggestionPriorityEmotion(User user, Resource resource, SuggestionReason reason) {

		// double priority = 0; 

		// switch(reason){

		// 	case NOT_VIEWED: 
		// 		//Here priority is normalize into (0.1) (!)todo: replace 5, and 0 to min and max depending on the values of emotion and cognitive load
		// 		priority = ((1-(currentEmotion-cognitiveLoad))-5)/5;

		// 		//Since we are concentrated in unseen items, every item that has been interacted with already receieves a priority of 0.
		// 	default: 
		// 		priority = 0; 	

		// }

	// 	return priority;
	// }
	
	@Override
	public SuggestionReason getSuggestionReason(User user, Resource resource) {


		if (resource instanceof CompletableResource) {
			CompletableResource completable = (CompletableResource) resource;
			if (!hasInteraction(user, resource, "completed")) {
				return SuggestionReason.NOT_COMPLETED;
			} else {
				double maxGrade = getBestGrade(user, completable);
				if (maxGrade < 1) {
					return SuggestionReason.NOT_MAX_GRADE;
				} else {
					return SuggestionReason.NOT_SUGGESTED;
				}
			}
		} else {
			if (!hasInteraction(user, resource, "viewed")) {
				return SuggestionReason.NOT_VIEWED;
			} else {
				return SuggestionReason.NOT_SUGGESTED;
			}
		}
	}
	
	public double getBestGrade(User user, CompletableResource completable) {
		double result = 0;
		try {
			return SPARQLConnection.getInstance().getBestGrade(user.getUserid(), completable.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public double getCognitiveLoad(Resource resource) {
		double result = 0; 
		try {
			result = SPARQLConnection.getInstance().getCognitiveLoad(resource.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public boolean hasInteraction(User user, Resource resource, String interactionName) {
		boolean result = false;
		System.out.println("--DEBUG: Proving wether the item "+ resource.getName()+ " has been seen by: "+ user.getName()+"userid"+ user.getUserid()+ " with interaction: "+ interactionName+" with resourceid "+ resource.getId());
		try {
			System.out.println("--DEBUG: Attemting to look for interaction in SPARQL: ");
			ArrayList<String> interactions = SPARQLConnection.getInstance().getInteractions(user.getUserid(), resource.getId());
			for (String interaction : interactions) {
				System.out.println("--DEBUG: looking at interaction "+ interaction+ "comparing to interactionname  "+interactionName);
				System.out.println(interaction.contains(interactionName));
				if (interaction.contains(interactionName)) { //TODO: change to equals
					result = true;
				}
			}
		} catch (Exception e) {
			System.out.println("--DEBUG: Somthing went wrong looking for the intaraction of a user");
			e.printStackTrace();
		}
		return result;
	}
	
	
}
