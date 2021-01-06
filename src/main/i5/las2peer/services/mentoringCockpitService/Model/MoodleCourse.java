package i5.las2peer.services.mentoringCockpitService.Model;

import java.time.Instant;

import i5.las2peer.services.mentoringCockpitService.MentoringCockpitService;
import i5.las2peer.services.mentoringCockpitService.Links.Completed;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.Quiz;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.Resource;
import i5.las2peer.services.mentoringCockpitService.SuggestionEvaluators.MoodleSuggestionEvaluator;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

public class MoodleCourse extends Course {

	public MoodleCourse(String courseid, String courseURL, MentoringCockpitService service) {
		super(courseid, courseURL, service);
		this.suggestionEvaluator = new MoodleSuggestionEvaluator();
	}
	
	@Override
	public void updateKnowledgeBase(long since) {
		setTimeToCurrent();
		createUsers(since);
		createResources(since);
		createThemes(since);
		createLinks(since);
	}

	@Override
	public String getSuggestion(String email, String courseid) {
		updateKnowledgeBase(lastUpdated);
		return users.get(email).getSuggestion(courseid);
	}
	
	@Override
	public void createUsers(long since) {
		// Match
		JSONObject match = new JSONObject();
		match.put("statement.context.extensions.https://tech4comp&46;de/xapi/context/extensions/courseInfo.courseid", Integer.parseInt(courseid));
		JSONObject gtObject = new JSONObject();
		gtObject.put("$gt", Instant.ofEpochSecond(since).toString());
		match.put("statement.stored", gtObject);
		JSONObject matchObj = new JSONObject();
		matchObj.put("$match", match);
		System.out.println("DEBUG --- Query: " + gtObject.getAsString("$gt"));
		
		
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
		
		String res = service.LRSconnect(sb.toString());
		
		System.out.println("DEBUG --- Users: " + res);
		
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		try {
			JSONArray data = (JSONArray) parser.parse(res);
			for (int i = 0; i < data.size(); i++) {
				JSONObject userObj = (JSONObject) data.get(i);
				users.put(userObj.getAsString("email"), new MoodleUser(userObj.getAsString("email"), userObj.getAsString("name"), this));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void createResources(long since) {
		// Match
		JSONObject match = new JSONObject();
		match.put("statement.context.extensions.https://tech4comp&46;de/xapi/context/extensions/courseInfo.courseid", Integer.parseInt(courseid));
		JSONObject gtObject = new JSONObject();
		gtObject.put("$gt", Instant.ofEpochSecond(since).toString());
		match.put("statement.stored", gtObject);
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
		JSONObject nameObject = new JSONObject();
		nameObject.put("$first", "$object.definition.name.en-US");
		group.put("_id", "$_id");
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
		
		String res = service.LRSconnect(sb.toString());
		
		//System.out.println("DEBUG --- Resources: " + res);
		
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		try {
			JSONArray data = (JSONArray) parser.parse(res);
			//System.out.println("DEBUG --- Size: " + data.size());
			for (int i = 0; i < data.size(); i++) {
				JSONObject resourceObj = (JSONObject) data.get(i);
				if (resourceObj.getAsString("_id").contains("quiz")) {
					resources.put(resourceObj.getAsString("_id"), new Quiz(resourceObj.getAsString("_id"), resourceObj.getAsString("name"), resourceObj.getAsString("_id"), 10));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void createThemes(long since) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createLinks(long since) {
		// Match
		JSONObject match = new JSONObject();
		match.put("statement.context.extensions.https://tech4comp&46;de/xapi/context/extensions/courseInfo.courseid", Integer.parseInt(courseid));
		JSONObject matchObj = new JSONObject();
		JSONObject gtObject = new JSONObject();
		gtObject.put("$gt", Instant.ofEpochSecond(since).toString());
		match.put("statement.stored", gtObject);
		matchObj.put("$match", match);
		
		// Project
		JSONObject project = new JSONObject();
		JSONObject idObject = new JSONObject();
		idObject.put("userid", "$statement.actor.account.name");
		idObject.put("verb", "$statement.verb.id");
		idObject.put("result", "$statement.result");
		idObject.put("timestamp", "$statement.stored");
		idObject.put("objectid", "$statement.object.id");
		project.put("_id", idObject);
		JSONObject projectObj = new JSONObject();
		projectObj.put("$project", project);
		
		// Group
		JSONObject groupObject = new JSONObject();
		JSONObject group = new JSONObject();
		group.put("_id", "$_id");
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
		
		//System.out.println("DEBUG --- Relations: " + res);
		
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		try {
			JSONArray data = (JSONArray) parser.parse(res);
			//System.out.println("DEBUG --- Size: " + data.size());
			for (int i = 0; i < data.size(); i++) {
				JSONObject dataObj = (JSONObject) data.get(i);
				JSONObject relationObj = (JSONObject) dataObj.get("_id");
				MoodleUser user = users.get(relationObj.getAsString("userid"));
				Resource resource = resources.get(relationObj.getAsString("objectid"));
				String verb = relationObj.getAsString("verb");
				long timestamp = Instant.parse(relationObj.getAsString("timestamp")).getEpochSecond();
				
				//TODO: Add more verbs
				if (verb.contains("completed")) {
					JSONObject resultObject = (JSONObject) relationObj.get("result");
					if (Boolean.parseBoolean(resultObject.getAsString("completion"))) {
						Quiz quiz = (Quiz) resource;
						JSONObject scoreObject = (JSONObject) resultObject.get("score");
						Completed completedLink = new Completed(timestamp, user, quiz, Double.parseDouble(scoreObject.getAsString("scaled")));
						user.getCompletedResources().put(quiz.getId(), completedLink);
						quiz.getUsers().put(user.getEmail(), completedLink);
					}
				} else if (verb.contains("")) {
					
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
