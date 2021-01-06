package i5.las2peer.services.mentoringCockpitService.Links;

public class Link {
	protected long timestamp;
	
	public Link (long timestamp) {
		this.timestamp = timestamp;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}
