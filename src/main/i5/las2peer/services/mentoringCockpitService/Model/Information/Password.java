package i5.las2peer.services.mentoringCockpitService.Model.Information;

public class Password extends Information {
	private String password;
	
	public Password(String password) {
		super("password");
		this.password = password;
	}

	@Override
	public String getInfoText() {
		return "Password: " + password;
	}

}
