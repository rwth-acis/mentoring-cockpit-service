package i5.las2peer.services.mentoringCockpitService.Themes;

public class Timestamp extends Information {
	String timestamp;
	
	public Timestamp(String timestamp) {
		super("timestamp");
		this.timestamp = timestamp;
	}

	@Override
	public String getInfoText() {
		return "Minute " + timestamp;
	}
}
