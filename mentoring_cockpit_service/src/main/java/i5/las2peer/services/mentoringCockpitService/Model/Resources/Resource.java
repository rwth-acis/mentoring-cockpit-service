package i5.las2peer.services.mentoringCockpitService.Model.Resources;

import i5.las2peer.services.mentoringCockpitService.Suggestion.SuggestionReason;

public abstract class Resource {
	protected String id;
	protected String name;
	protected String url;
	protected String type;
	
	public Resource(String id, String name, String url, String type) {
		this.id = id;
		this.name = name;
		this.url = url;
		this.type = type;
	}
	
	public abstract String getSuggestionText(SuggestionReason reason, boolean html);
	
	public abstract String getSuggestionItemText(boolean html);
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}	
