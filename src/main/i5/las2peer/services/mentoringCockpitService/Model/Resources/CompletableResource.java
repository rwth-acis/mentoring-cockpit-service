package i5.las2peer.services.mentoringCockpitService.Model.Resources;

public abstract class CompletableResource extends Resource {
	double minGrade;
	double maxGrade;
	double passingGrade;
	
	public CompletableResource(String id, String name, String url, double minGrade, double maxGrade,
			double passingGrade) {
		super(id, name, url);
		this.minGrade = minGrade;
		this.maxGrade = maxGrade;
		this.passingGrade = passingGrade;
	}
	
	
}
