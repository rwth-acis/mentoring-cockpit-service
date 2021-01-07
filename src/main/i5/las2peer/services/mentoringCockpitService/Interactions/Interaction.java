package i5.las2peer.services.mentoringCockpitService.Interactions;

public class Interaction {
	protected long timestamp;
	protected String name;
	
	public Interaction (long timestamp, String name) {
		this.timestamp = timestamp;
		this.name = name;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getName() {
		return name;
	}
}
