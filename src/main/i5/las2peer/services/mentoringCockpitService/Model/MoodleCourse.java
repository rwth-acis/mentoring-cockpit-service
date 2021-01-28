package i5.las2peer.services.mentoringCockpitService.Model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;

import i5.las2peer.services.mentoringCockpitService.MentoringCockpitService;
import i5.las2peer.services.mentoringCockpitService.Interactions.Completed;
import i5.las2peer.services.mentoringCockpitService.Interactions.UserResourceInteraction;
import i5.las2peer.services.mentoringCockpitService.Interactions.Viewed;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.CompletableResource;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.File;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.Quiz;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.Resource;
import i5.las2peer.services.mentoringCockpitService.Suggestion.MoodleSuggestionEvaluator;
import i5.las2peer.services.mentoringCockpitService.Suggestion.Suggestion;
import i5.las2peer.services.mentoringCockpitService.Suggestion.TextFormatter;
import i5.las2peer.services.mentoringCockpitService.Themes.Theme;
import i5.las2peer.services.mentoringCockpitService.Themes.ThemeResourceLink;
import i5.las2peer.services.mentoringCockpitService.Model.Resources.Hyperlink;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

public class MoodleCourse extends Course {

	public MoodleCourse(String courseid, String courseURL, MentoringCockpitService service) {
		super(courseid, courseURL, service, new MoodleSuggestionEvaluator(0, 1));
	}
	
	@Override
	public void updateKnowledgeBase(long since) {
		setTimeToCurrent();
		newResources.clear();
		
		createResources(since);
		createUsers(since);
		createInteractions(since);
		createThemes(since);
	}

	@Override
	public String getSuggestion(String userid, int numOfSuggestions) {
		updateKnowledgeBase(lastUpdated);
		String result = "";
		if (users.containsKey(userid)) {
			ArrayList<Suggestion> suggestions =  users.get(userid).getSuggestion(numOfSuggestions);
			ArrayList<String> suggestionTexts = new ArrayList<String>();
			for (Suggestion suggestion : suggestions) {
				suggestionTexts.add(suggestion.getSuggestionText());
			}
			
			if (!suggestions.isEmpty()) {
				//System.out.println("DEBUG --- Priority: " + suggestion.getPriority());
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
			String resourceText = themes.get(themeid).getResourceText();
			String subthemeText = themes.get(themeid).getSubthemeText();
			
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
		match.put("statement.context.extensions.https://tech4comp&46;de/xapi/context/extensions/courseInfo.courseid", Integer.parseInt(courseid));
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
		JSONObject projectObj = new JSONObject();
		projectObj.put("$project", project);
		
		// Group
		JSONObject groupObject = new JSONObject();
		JSONObject group = new JSONObject();
		JSONObject idObject = new JSONObject();
		JSONObject nameObject = new JSONObject();
		idObject.put("$first", "$userid");
		nameObject.put("$first", "$name");
		group.put("_id", "$_id");
		group.put("userid", idObject);
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
		
		//System.out.println("DEBUG --- Users: " + res);
		
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		try {
			JSONArray data = (JSONArray) parser.parse(res);
			for (int i = 0; i < data.size(); i++) {
				JSONObject userObj = (JSONObject) data.get(i);
				if (!users.containsKey(userObj.getAsString("userid"))) {
					users.put(userObj.getAsString("userid"), new MoodleUser(userObj.getAsString("userid"), userObj.getAsString("name"), this));
				}
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
		
		String query = "PREFIX ulo: <http://uni-leipzig.de/tech4comp/ontology/>\r\n" + 
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
				"    SELECT DISTINCT ?resourceid WHERE {\r\n" + 
				"  		GRAPH <http://triplestore.tech4comp.dbis.rwth-aachen.de/Wissenslandkarten/data/Moodle_18> {\r\n" + 
				"    		?b a ulo:Material .\r\n" + 
				"    		?b ulo:id ?resourceid .\r\n" + 
				"  		} \r\n" + 
				"    } ";
		
		try {
			String response = sparqlQuery(query);
			JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
			JSONObject responseObj = (JSONObject) parser.parse(response.toString());
			JSONObject resultsObj = (JSONObject) responseObj.get("results");
			JSONArray bindingsArray = (JSONArray) resultsObj.get("bindings");
			HashSet<String> resourceIds = new HashSet<String>();
			for (int i = 0; i < bindingsArray.size(); i++) {
				JSONObject bindingObj = (JSONObject) bindingsArray.get(i);
				resourceIds.add(((JSONObject) bindingObj.get("resourceid")).getAsString("value"));
			}
			
			JSONArray data = (JSONArray) parser.parse(res);
			//System.out.println("DEBUG --- Size: " + data.size());
			for (int i = 0; i < data.size(); i++) {
				JSONObject resourceObj = (JSONObject) data.get(i);
				if (resourceIds.contains(resourceObj.getAsString("_id"))) {
					if (resourceObj.getAsString("_id").contains("quiz") && !resourceObj.getAsString("name").contains("attempt")) {
						resources.put(resourceObj.getAsString("_id"), new Quiz(resourceObj.getAsString("_id"), resourceObj.getAsString("name"), resourceObj.getAsString("_id")));
					} else if (resourceObj.getAsString("_id").contains("resource")) {
						resources.put(resourceObj.getAsString("_id"), new File(resourceObj.getAsString("_id"), resourceObj.getAsString("name"), resourceObj.getAsString("_id")));
					} else if (resourceObj.getAsString("_id").contains("url")) {
						resources.put(resourceObj.getAsString("_id"), new Hyperlink(resourceObj.getAsString("_id"), resourceObj.getAsString("name"), resourceObj.getAsString("_id")));
					}
				}	
			}
			//System.out.println("DEBUG --- Resources: " + resources.keySet().toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void createThemes(long since) {
		
		// First, create all themes
		try {
			String query = "PREFIX ulo: <http://uni-leipzig.de/tech4comp/ontology/>\r\n" + 
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
					"	\r\n" + 
					"    SELECT ?themeid ?name WHERE {\r\n" + 
					"  		GRAPH <http://triplestore.tech4comp.dbis.rwth-aachen.de/Wissenslandkarten/data/%s> {\r\n" + 
					"  			?themeid a ulo:Theme .  \r\n" + 
					"  			?themeid rdfs:label ?name .  \r\n" + 
					"		}\r\n" + 
					"    }";
			
			String response = sparqlQuery(query);
			
			JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
			JSONObject responseObj = (JSONObject) parser.parse(response.toString());
			JSONObject resultsObj = (JSONObject) responseObj.get("results");
			JSONArray bindingsArray = (JSONArray) resultsObj.get("bindings");
			for (int i = 0; i < bindingsArray.size(); i++) {
				JSONObject bindingObj = (JSONObject) bindingsArray.get(i);
				String themeid = ((JSONObject) bindingObj.get("themeid")).getAsString("value");
				String name = ((JSONObject) bindingObj.get("name")).getAsString("value");
				themes.put(themeid, new Theme(themeid, name));
				
			}
			//System.out.println("DEBUG --- URIS: " + themeids.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Then, create theme structure
		try {
			String query = "PREFIX ulo: <http://uni-leipzig.de/tech4comp/ontology/>\r\n" + 
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
					"	\r\n" + 
					"    SELECT ?supertheme ?subtheme WHERE {\r\n" + 
					"  		GRAPH <http://triplestore.tech4comp.dbis.rwth-aachen.de/Wissenslandkarten/data/%s> {\r\n" + 
					"  			?supertheme ulo:superthemeOf ?subtheme .  \r\n" + 
					"		}\r\n" + 
					"    }";
			
			String response = sparqlQuery(query);
			
			JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
			JSONObject responseObj = (JSONObject) parser.parse(response.toString());
			JSONObject resultsObj = (JSONObject) responseObj.get("results");
			JSONArray bindingsArray = (JSONArray) resultsObj.get("bindings");
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
			String query = "PREFIX ulo: <http://uni-leipzig.de/tech4comp/ontology/>\r\n" + 
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
					"    SELECT ?themeid ?resourceid ?infoType ?infoVal WHERE {\r\n" + 
					"  		GRAPH <http://triplestore.tech4comp.dbis.rwth-aachen.de/Wissenslandkarten/data/%s> {\r\n" + 
					"    		?themeid ulo:continuativeMaterial ?s1 .\r\n" + 
					"  			?s1 ulo:id ?resourceid .\r\n" + 
					"    		?s1 ?infoType ?infoVal .\r\n" + 
					"  		} \r\n" + 
					"    } ";
			
			String response = sparqlQuery(query);
			
			JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
			JSONObject responseObj1 = (JSONObject) parser.parse(response.toString());
			JSONObject resultsObj1 = (JSONObject) responseObj1.get("results");
			JSONArray bindingsArray1 = (JSONArray) resultsObj1.get("bindings");
			for (int i = 0; i < bindingsArray1.size(); i++) {
				JSONObject bindingObj = (JSONObject) bindingsArray1.get(i);
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
	
	private String sparqlQuery(String query) {
		try {
			URL url = new URL(service.triplestoreDomain);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/sparql-query");
			conn.setRequestProperty("Accept", "application/json");
			conn.setDoOutput(true);
			
			try(OutputStream os = conn.getOutputStream()) {
			    byte[] input = String.format(query, "Moodle_" + courseid).getBytes("utf-8");
			    os.write(input, 0, input.length);			
			}
			StringBuilder response = new StringBuilder();
			try(BufferedReader br = new BufferedReader(
					  new InputStreamReader(conn.getInputStream(), "utf-8"))) {
					    String responseLine = null;
					    while ((responseLine = br.readLine()) != null) {
					        response.append(responseLine.trim());
					    }
					    //System.out.println("DEBUG --- RESPONSE: " + response.toString());
					}
			return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "SPARQL connection failed";
		}
	}

	@Override
	public void createInteractions(long since) {
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
				User user = users.get(relationObj.getAsString("userid"));
				Resource resource = resources.get(relationObj.getAsString("objectid"));
				String verb = relationObj.getAsString("verb");
				long timestamp = Instant.parse(relationObj.getAsString("timestamp")).getEpochSecond();
				
				if (resource != null) {
					//TODO: Add more verbs
					UserResourceInteraction interaction = null;
					if (verb.contains("completed")) {
						JSONObject resultObject = (JSONObject) relationObj.get("result");
						if (Boolean.parseBoolean(resultObject.getAsString("completion"))) {
							CompletableResource completableResource = (CompletableResource) resource;
							JSONObject scoreObject = (JSONObject) resultObject.get("score");
							interaction = new Completed(timestamp, user, completableResource, Double.parseDouble(scoreObject.getAsString("scaled")));
						}
					} else if (verb.contains("viewed")) {
						interaction = new Viewed(timestamp, user, resource);
					}
					
					// Add interactions to interaction lists
					if (interaction != null) {
						if (!user.getInteractionLists().containsKey(resource.getId())) {
							user.getInteractionLists().put(resource.getId(), new ArrayList<UserResourceInteraction>());
						}
						user.getInteractionLists().get(resource.getId()).add(interaction);
					}
					
					// Add resource to user's newly interacted resource list
					user.getRecentlyInteractedResources().add(resource);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
