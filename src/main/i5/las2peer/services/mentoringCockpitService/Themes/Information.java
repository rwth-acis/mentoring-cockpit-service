package i5.las2peer.services.mentoringCockpitService.Themes;

public abstract class Information {
	String name;
	
	public Information(String name) {
		this.name = name;
	}
	
	public abstract String getInfoText();
}
