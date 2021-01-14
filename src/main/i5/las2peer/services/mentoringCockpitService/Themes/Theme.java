package i5.las2peer.services.mentoringCockpitService.Themes;

import java.util.HashMap;

public class Theme {
	String themeid;
	HashMap<String, ThemeResourceLink> resourceLinks;
	HashMap<String, Theme> subthemes;
	
	public Theme(String themeid) {
		this.themeid = themeid;
		this.resourceLinks = new HashMap<String, ThemeResourceLink>();
		this.subthemes = new HashMap<String, Theme>();
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

}
