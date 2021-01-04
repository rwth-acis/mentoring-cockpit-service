package i5.las2peer.services.mentoringCockpitService.Model;

import i5.las2peer.services.mentoringCockpitService.MentoringCockpitService;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.Quiz;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

public class MoodleCourse extends Course {

	public MoodleCourse(String courseid, String courseURL, MentoringCockpitService service) {
		super(courseid, courseURL, service);
	}

	@Override
	public void createUsers() {
		// Match
		JSONObject match = new JSONObject();
		match.put("statement.context.extensions.https://tech4comp&46;de/xapi/context/extensions/courseInfo.courseid", Integer.parseInt(courseid));
		JSONObject matchObj = new JSONObject();
		matchObj.put("$match", match);
		
		// Project
		JSONObject project = new JSONObject();
		project.put("_id", "$statement.actor.account.name");
		project.put("email", "$statement.actor.account.name");
		project.put("name", "$statement.actor.name");
		JSONObject projectObj = new JSONObject();
		projectObj.put("$project", project);
		
		// Group
		JSONObject groupObject = new JSONObject();
		JSONObject group = new JSONObject();
		JSONObject emailObject = new JSONObject();
		JSONObject nameObject = new JSONObject();
		emailObject.put("$first", "$email");
		nameObject.put("$first", "$name");
		group.put("_id", "$_id");
		group.put("email", emailObject);
		group.put("name", nameObject);
		groupObject.put("$group", group);
		
		// Assemble pipeline
		JSONArray pipeline = new JSONArray();
		pipeline.add(matchObj);
		pipeline.add(projectObj);
		pipeline.add(groupObject);
		
		StringBuilder sb = new StringBuilder();
		for (byte b : pipeline.toString().getBytes()) {
			sb.append("%" + String.format("%02X", b));
		}
		//System.out.println("DEBUG --- Query: " + pipeline.toString());
		String res = service.LRSconnect(sb.toString());
		
		System.out.println("DEBUG --- Users: " + res);
		
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		try {
			JSONArray data = (JSONArray) parser.parse(res);
			for (int i = 0; i < data.size(); i++) {
				JSONObject userObj = (JSONObject) data.get(i);
				users.put(userObj.getAsString("email"), new User(userObj.getAsString("email"), userObj.getAsString("name")));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void createResources() {
		// Match
		JSONObject match = new JSONObject();
		match.put("statement.context.extensions.https://tech4comp&46;de/xapi/context/extensions/courseInfo.courseid", Integer.parseInt(courseid));
		JSONObject matchObj = new JSONObject();
		matchObj.put("$match", match);
		
		// Project
		JSONObject project = new JSONObject();
		project.put("_id", "$statement.object.id");
		project.put("object", "$statement.object");
		JSONObject projectObj = new JSONObject();
		projectObj.put("$project", project);
		
		// Group
		JSONObject groupObject = new JSONObject();
		JSONObject group = new JSONObject();
		JSONObject resourceObject = new JSONObject();
		resourceObject.put("$first", "$object");
		group.put("_id", "$_id");
		group.put("object", resourceObject);
		groupObject.put("$group", group);
		
		// Assemble pipeline
		JSONArray pipeline = new JSONArray();
		pipeline.add(matchObj);
		pipeline.add(projectObj);
		pipeline.add(groupObject);
		
		StringBuilder sb = new StringBuilder();
		for (byte b : pipeline.toString().getBytes()) {
			sb.append("%" + String.format("%02X", b));
		}
		
		String res = service.LRSconnect(sb.toString());
		
		System.out.println("DEBUG --- Resources: " + res);
		
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		try {
			JSONArray data = (JSONArray) parser.parse(res);
			System.out.println("DEBUG --- Size: " + data.size());
			for (int i = 0; i < data.size(); i++) {
				JSONObject resourceObj = (JSONObject) data.get(i);
				if (resourceObj.getAsString("_id").contains("quiz")) {
					resources.put(resourceObj.getAsString("_id"), new Quiz(resourceObj.getAsString("_id"), "", resourceObj.getAsString("_id"), 10));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void createThemes() {
		// TODO Auto-generated method stub
		
	}

}
