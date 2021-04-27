package i5.las2peer.services.mentoringCockpitService.Model.Information;

public abstract class Information {
	String name;
	
	public Information(String name) {
		this.name = name;
	}
	
	public abstract String getInfoText();
}
