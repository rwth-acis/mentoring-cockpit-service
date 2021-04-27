package i5.las2peer.services.mentoringCockpitService.Model.Information;

public class Pages extends Information {
	String pageNumbers;
	
	public Pages(String pageNrs) {
		super("pages");
		this.pageNumbers = pageNrs;
	}

	@Override
	public String getInfoText() {
		if (pageNumbers.contains("-")) {
			return "Pages " + pageNumbers.split("-")[0] + " to " + pageNumbers.split("-")[1];
		} else {
			return "Page " + pageNumbers;
		}
	}
	
	
	
}
