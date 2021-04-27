package i5.las2peer.services.mentoringCockpitService.Model;


import java.time.Instant;
import java.util.ArrayList;

import i5.las2peer.services.mentoringCockpitService.MentoringCockpitService;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.CompletableResource;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.File;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.Quiz;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.Resource;
import i5.las2peer.services.mentoringCockpitService.SPARQLConnection.SPARQLConnection;
import i5.las2peer.services.mentoringCockpitService.Suggestion.MoodleSuggestionEvaluator;
import i5.las2peer.services.mentoringCockpitService.Suggestion.Suggestion;
import i5.las2peer.services.mentoringCockpitService.Suggestion.TextFormatter;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.Hyperlink;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

public class MoodleCourse extends Course {

	public MoodleCourse(String courseid, String courseURL, MentoringCockpitService service) {
		super(courseid, courseURL, service, new MoodleSuggestionEvaluator(0, 1));
	}
	
	@Override
	public void update() {
		long since = lastUpdated;
		setTimeToCurrent();
		newResources.clear();
		
		updateOntology(since);
		updateProfiles(since);
	}
	
	@Override
	protected void updateOntology(long since) {
		if (since == 0) {
			try {
				SPARQLConnection.getInstance().addSchema();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			since = since + 1;
		}
		createResources(since);
		createUsers(since);
		createInteractions(since);
		createThemes(since);
	}
	
	protected void updateProfiles(long since) {
		try {
			JSONArray updates = SPARQLConnection.getInstance().getUpdates(since, courseid);
			for (int i = 0; i < updates.size(); i++) {
				JSONObject obj = (JSONObject) updates.get(i);
				String userid = ((JSONObject) obj.get("userid")).getAsString("value");
				String username = ((JSONObject) obj.get("username")).getAsString("value");
				String resourceid = ((JSONObject) obj.get("resourceid")).getAsString("value");
				String resourcename = ((JSONObject) obj.get("resourcename")).getAsString("value");
				String resourcetype = ((JSONObject) obj.get("resourcetype")).getAsString("value");
				
				if (!users.containsKey(userid)) {
					users.put(userid, new User(userid, username, resources.values()));
				}
				
				Resource resource = null;
				if (!resources.containsKey(resourceid)) {
					if (resourcetype.contains("file")) {
						resource = new File(resourceid, resourcename, resourceid);
					} else if (resourcetype.contains("hyperlink")) {
						resource = new Hyperlink(resourceid, resourcename, resourceid);
					} else if (resourcetype.contains("quiz")) {
						resource = new Quiz(resourceid, resourcename, resourceid);
					}
					if (resource != null) {
						resources.put(resourceid, resource);
						newResources.add(resource);
					}
				} else {
					resource = resources.get(resourceid);
				}
				if (resource != null) {
					users.get(userid).getUpdateSet().add(resource);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String getSuggestion(String userid, int numOfSuggestions) {
		String result = "";
		if (users.containsKey(userid)) {
			users.get(userid).updateSuggestions(newResources);
			ArrayList<Suggestion> suggestions =  users.get(userid).getSuggestion(numOfSuggestions);
			ArrayList<String> suggestionTexts = new ArrayList<String>();
			for (Suggestion suggestion : suggestions) {
				suggestionTexts.add(suggestion.getSuggestionText());
			}
			
			if (!suggestions.isEmpty()) {
				result = "Here is a couple suggestions based on your Moodle activity:" + TextFormatter.createList(suggestionTexts) + "\n Would you like another suggestion?";
			} else {
				result = "No suggestions available";
			}
		} else {
			result = "Error: User not initialized!";
		}
		return result;
	}

	@Override
	public String getThemeSuggestions(String shortid) {
		String themeid = "http://halle/domainmodel/" + shortid;
		String result = "";
		if (themes.containsKey(themeid)) {
			String resourceText = themes.get(themeid).getResourceSuggestions();
			String subthemeText = themes.get(themeid).getThemeSuggestions();
			
			if (!resourceText.equals("")) {
				result = result + "The following resources are related to the theme " + TextFormatter.quote(themes.get(themeid).getName()) + ":" + resourceText;
			}
			if (!subthemeText.equals("")) {
				result = result + "Reply with one of the following related themes if you would like to know more about it:" + subthemeText;
			}
		} else {
			result = "Error: Theme not initialized!";
		}
		return result;
	}

	@Override
	public void createUsers(long since) {
		// Match
		JSONObject match = new JSONObject();
		match.put("statement.context.extensions.https://tech4comp&46;de/xapi/context/extensions/courseInfo.courseid", Integer.parseInt(courseid.split("id=")[1]));
		JSONObject gtObject = new JSONObject();
		gtObject.put("$gt", Instant.ofEpochSecond(since).toString());
		match.put("statement.stored", gtObject);
		JSONObject matchObj = new JSONObject();
		matchObj.put("$match", match);
		
		// Project
		JSONObject project = new JSONObject();
		project.put("_id", "$statement.actor.account.name");
		project.put("userid", "$statement.actor.account.name");
		project.put("name", "$statement.actor.name");
		project.put("roles", "$statement.context.extensions.https://tech4comp&46;de/xapi/context/extensions/actorRoles");
		JSONObject projectObj = new JSONObject();
		projectObj.put("$project", project);
		
		// Group
		JSONObject groupObject = new JSONObject();
		JSONObject group = new JSONObject();
		JSONObject idObject = new JSONObject();
		JSONObject nameObject = new JSONObject();
		JSONObject roleObject = new JSONObject();
		idObject.put("$first", "$userid");
		nameObject.put("$first", "$name");
		roleObject.put("$first", "$roles");
		group.put("_id", "$_id");
		group.put("userid", idObject);
		group.put("name", nameObject);
		group.put("roles", roleObject);
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
		
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		try {
			JSONArray data = (JSONArray) parser.parse(res);
			JSONArray usersArray = new JSONArray();
			for (int i = 0; i < data.size(); i++) {
				JSONObject userObj = (JSONObject) data.get(i);
				userObj.put("courseid", courseid);
				usersArray.add(userObj);
			}
			SPARQLConnection.getInstance().addUser(usersArray);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void createResources(long since) {
		// Match
		JSONObject match = new JSONObject();
		match.put("statement.context.extensions.https://tech4comp&46;de/xapi/context/extensions/courseInfo.courseid", Integer.parseInt(courseid.split("id=")[1]));
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
		
		
		try {
			JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
			JSONArray data = (JSONArray) parser.parse(res);
			JSONArray resourcesArray = new JSONArray();
			for (int i = 0; i < data.size(); i++) {
				JSONObject resourceObj = (JSONObject) data.get(i);
				if (resourceObj.getAsString("_id").contains("quiz") && !resourceObj.getAsString("name").contains("attempt")) {
					resourceObj.put("type", "quiz");
				} else if (resourceObj.getAsString("_id").contains("resource")) {
					resourceObj.put("type", "file");
				} else if (resourceObj.getAsString("_id").contains("url")) {
					resourceObj.put("type", "hyperlink");
				} else if (resourceObj.getAsString("_id").contains("forum/view")) {
					resourceObj.put("type", "forum");
				}else if (resourceObj.getAsString("_id").contains("forum/discuss")) {
					resourceObj.put("type", "post");
				} else {
					resourceObj.put("type", "undefined");
				}
				resourceObj.put("courseid", courseid);
				resourcesArray.add(resourceObj);
			}	
			SPARQLConnection.getInstance().addResources(resourcesArray);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void createThemes(long since) {
		
		// First, create all themes
		try {
			JSONArray bindingsArray = SPARQLConnection.getInstance().getThemes();

			for (int i = 0; i < bindingsArray.size(); i++) {
				JSONObject bindingObj = (JSONObject) bindingsArray.get(i);
				String themeid = ((JSONObject) bindingObj.get("themeid")).getAsString("value");
				String name = ((JSONObject) bindingObj.get("name")).getAsString("value");
				themes.put(themeid, new Theme(themeid, name));
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Then, create theme structure
		try {
			JSONArray bindingsArray = SPARQLConnection.getInstance().getThemeStructure();
			for (int i = 0; i < bindingsArray.size(); i++) {
				JSONObject bindingObj = (JSONObject) bindingsArray.get(i);
				JSONObject subjectObj = (JSONObject) bindingObj.get("supertheme");
				JSONObject objectObj = (JSONObject) bindingObj.get("subtheme");
				if (themes.get(subjectObj.getAsString("value")) != null && themes.get(objectObj.getAsString("value")) != null) {
					themes.get(subjectObj.getAsString("value")).addSubtheme(themes.get(objectObj.getAsString("value")));
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Finally, assign resources and resource information
		try {
			JSONArray bindingsArray = SPARQLConnection.getInstance().getThemesInfo();
			for (int i = 0; i < bindingsArray.size(); i++) {
				JSONObject bindingObj = (JSONObject) bindingsArray.get(i);
				String themeid = ((JSONObject) bindingObj.get("themeid")).getAsString("value");
				String resourceid = ((JSONObject) bindingObj.get("resourceid")).getAsString("value");
				if (resources.containsKey(resourceid)) {
					if (!themes.get(themeid).getResourceLinks().containsKey(resourceid)) {
						themes.get(themeid).getResourceLinks().put(resourceid, new ThemeResourceLink(resources.get(resourceid)));
						if (resources.get(resourceid) instanceof CompletableResource) {
							((CompletableResource)resources.get(resourceid)).addTheme(themes.get(themeid));
						}
					}
					
					String infoType = ((JSONObject) bindingObj.get("infoType")).getAsString("value");
					String infoVal = ((JSONObject) bindingObj.get("infoVal")).getAsString("value");
					themes.get(themeid).getResourceLinks().get(resourceid).addInfo(infoType, infoVal);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	

	@Override
	public void createInteractions(long since) {
		// Match
		JSONObject match = new JSONObject();
		match.put("statement.context.extensions.https://tech4comp&46;de/xapi/context/extensions/courseInfo.courseid", Integer.parseInt(courseid.split("id=")[1]));
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
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		try {
			JSONArray data = (JSONArray) parser.parse(res);
			JSONArray interactions = new JSONArray();
			for (int i = 0; i < data.size(); i++) {
				JSONObject dataObj = (JSONObject) data.get(i);
				JSONObject relationObj = (JSONObject) dataObj.get("_id");
				String verb = relationObj.getAsString("verb");
				String verbShort = "";
				JSONObject infoObj = new JSONObject();
				if (verb.contains("completed")) {
					JSONObject resultObject = (JSONObject) relationObj.get("result");
					if (Boolean.parseBoolean(resultObject.getAsString("completion"))) {
						verbShort = "completed";
						JSONObject scoreObject = (JSONObject) resultObject.get("score");
						infoObj.put("score", scoreObject.getAsNumber("scaled"));
					}
				} else if (verb.contains("viewed")) {
					verbShort = "viewed";
				} else if (verb.contains("posted") || verb.contains("replied")) {
					verbShort = "posted";
				} else {
					verbShort = "interacted";
				}
				relationObj.put("info", infoObj);
				relationObj.put("verbShort", verbShort);
				relationObj.put("courseid", courseid);
				interactions.add(relationObj);
				
			}
			SPARQLConnection.getInstance().addInteractions(interactions);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
