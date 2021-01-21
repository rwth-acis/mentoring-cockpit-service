package i5.las2peer.services.mentoringCockpitService.Themes;

import java.util.ArrayList;
import java.util.HashMap;

import i5.las2peer.services.mentoringCockpitService.Suggestion.TextFormatter;

public class Theme {
	String themeid;
	String name;
	HashMap<String, ThemeResourceLink> resourceLinks;
	HashMap<String, Theme> subthemes;
	
	public Theme(String themeid, String name) {
		this.themeid = themeid;
		this.name = name;
		this.resourceLinks = new HashMap<String, ThemeResourceLink>();
		this.subthemes = new HashMap<String, Theme>();
	}

	public String getName() {
		return name;
	}

	public String getThemeid() {
		return themeid;
	}
	
	public void addResourceLink(String resourceid, ThemeResourceLink link) {
		resourceLinks.put(resourceid, link);
	}
	
	public HashMap<String, ThemeResourceLink> getResourceLinks() {
		return resourceLinks;
	}

	public void addSubtheme(Theme theme) {
		subthemes.put(theme.getThemeid(), theme);
	}
	
	public String getResourceText() {
		ArrayList<String> items = new ArrayList<String>();
		for (ThemeResourceLink link : resourceLinks.values()) {
			items.add(link.getSuggestionText());
		}
		if (!items.isEmpty()) {
			return TextFormatter.createList(items);
		} else {
			return "";
		}
		
	}
	
	public String getSubthemeText() {
		ArrayList<String> names = new ArrayList<String>();
		for (Theme subtheme : subthemes.values()) {
			names.add(subtheme.getName());
		}
		if (!names.isEmpty()) {
			return TextFormatter.createList(names);
		} else {
			return "";
		}
		
	}

}
