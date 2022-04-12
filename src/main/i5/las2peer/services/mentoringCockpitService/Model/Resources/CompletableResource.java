package i5.las2peer.services.mentoringCockpitService.Model.Resources;

import java.util.ArrayList;

import i5.las2peer.services.mentoringCockpitService.Model.Theme;
import i5.las2peer.services.mentoringCockpitService.Suggestion.SuggestionReason;
import i5.las2peer.services.mentoringCockpitService.Suggestion.TextFormatter;

public abstract class CompletableResource extends Resource {
	protected double minGrade;
	protected double maxGrade;
	protected double passingGrade;
	protected ArrayList<Theme> themes;
	
	public CompletableResource(String id, String name, String url, String type, double minGrade, double maxGrade,
			double passingGrade) {
		super(id, name, url, type);
		this.minGrade = minGrade;
		this.maxGrade = maxGrade;
		this.passingGrade = passingGrade;
		this.themes = new ArrayList<Theme>();
	}
	
	public void addTheme(Theme theme) {
		this.themes.add(theme);
	}
	
	@Override
	public String getSuggestionText(SuggestionReason reason) {
		switch(reason) {
			case NOT_COMPLETED:
				return "Here is this quiz :bar_chart: " + type + " " + TextFormatter.quote(TextFormatter.createHyperlink(name, url));
			case NOT_MAX_GRADE:
				ArrayList<String> resources = new ArrayList<String>();
				for (int i = 0 ; i < 3 ; i++) {
					resources.add(themes.get(i).getResourceTextForCompletable());
				}
				return "Here are some resources that can help you improve your grade in the " + type + " " + TextFormatter.quote(TextFormatter.createHyperlink(name, url)) + ":" +
						TextFormatter.createOrderedList(resources);
			default:
				return "Completable resource not implemented";
		}
	}
	
	
 }
