package i5.las2peer.services.mentoringCockpitService.Model;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import i5.las2peer.services.mentoringCockpitService.Suggestion.TextFormatter;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.Resource;

public class Theme {
	String themeid;
	String name;
	LinkedHashMap<String, ThemeResourceLink> resourceLinks;
	LinkedHashMap<String, Theme> subthemes;
	
	public Theme(String themeid, String name) {
		this.themeid = themeid;
		this.name = name;
		this.resourceLinks = new LinkedHashMap<String, ThemeResourceLink>();
		this.subthemes = new LinkedHashMap<String, Theme>();
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
	
	public String getResourceSuggestions(boolean html) {
		ArrayList<String> items = new ArrayList<String>();
		for (ThemeResourceLink link : resourceLinks.values()) {
			items.add(link.getSuggestionText(html));
		}
		if (!items.isEmpty()) {
			if (html) {
				return TextFormatter.createHTMLList(items);
			} else {
				return TextFormatter.createChatList(items);
			}
		} else {
			System.out.println("No ressources to theme " + this.name + " (id: " + this.themeid + ") found.\n" +
					"Theme has resources: " + this.resourceLinks);
			return "";
		}
		
	}
	
	public String getResourceTextForCompletable(boolean html) {
		return resourceLinks.entrySet().iterator().next().getValue().getSuggestionText(html);
		
	}
	
	public String getThemeSuggestions(boolean html) {
		ArrayList<String> names = new ArrayList<String>();
		for (Theme subtheme : subthemes.values()) {
			names.add(subtheme.getName());
		}
		if (!names.isEmpty()) {
			if (html) {
				return TextFormatter.createHTMLList(names);
			} else {
				return TextFormatter.createChatList(names);
			}
		} else {
			return "";
		}
		
	}

}
