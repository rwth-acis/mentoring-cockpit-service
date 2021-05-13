package i5.las2peer.services.mentoringCockpitService.Model.Information;

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
