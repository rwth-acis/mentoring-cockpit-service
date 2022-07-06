package i5.las2peer.services.mentoringCockpitService.SPARQLConnection;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

public class SPARQLConnection {
	private static SPARQLConnection instance;
	private String endpoint;
	private String graphurl;
	
	private SPARQLConnection(String endpoint) {
		this.endpoint = endpoint;
		this.graphurl = endpoint + "/data";
	}
	
	public static SPARQLConnection getInstance() throws Exception{
		if (instance != null) {
			return instance;
		} else {
			throw new Exception("SPARQL connection not started");
		}
	}
	
	public static void startConnection(String endpoint) {
		instance = new SPARQLConnection(endpoint);
	}
	
	public long getLatestTime(String courseid) {
		String query = "PREFIX ulo: <http://uni-leipzig.de/tech4comp/ontology/>\r\n" + 
				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n" + 
				"SELECT ?timestamp WHERE {\r\n" + 
				"    GRAPH <https://triplestore.tech4comp.dbis.rwth-aachen.de/LMSData/data> {\r\n" + 
				"        <" + courseid + "> ulo:hasResource ?resource .\r\n" + 
				"        ?b ulo:interactionResource ?resource .\r\n" + 
				"  	     ?b ulo:timestamp ?timestamp .\r\n" + 
				"    }\r\n" + 
				"} ORDER BY DESC(xsd:integer(?timestamp)) LIMIT 1";
		
		long res = 0;
		JSONArray bindings =  getBindings(sparqlQuery(query));
		if (!bindings.isEmpty()) {
			JSONObject obj = (JSONObject) bindings.get(0);
			JSONObject varObj = (JSONObject) obj.get("timestamp");
			res = varObj.getAsNumber("value").longValue();
		}
		return res;
	}
	
	public void addSchema() {
		String query = "PREFIX ulo: <http://uni-leipzig.de/tech4comp/ontology/>\r\n" + 
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
				"INSERT DATA {\r\n" + 
				"	GRAPH <%s> {\r\n" + 
				"    	ulo:viewed rdfs:subclassOf ulo:interaction .\r\n" + 
				"    	ulo:completed rdfs:subclassOf ulo:interaction .\r\n" + 
				"    	ulo:posted rdfs:subclassOf ulo:interaction .\r\n" + 
				"    	ulo:interacted rdfs:subclassOf ulo:interaction .\r\n" + 
				"    	ulo:timestamp rdfs:subclassOf ulo:information .\r\n" + 
				"    	ulo:password rdfs:subclassOf ulo:information .\r\n" + 
				"    	ulo:pages rdfs:subclassOf ulo:information .\r\n" + 
				"    	ulo:file rdfs:subclassOf ulo:resource .\r\n" + 
				"    	ulo:hyperlink rdfs:subclassOf ulo:resource .\r\n" + 
				"    	ulo:quiz rdfs:subclassOf ulo:resource .\r\n" + 
				"    	ulo:forum rdfs:subclassOf ulo:resource .\r\n" + 
				"    	ulo:post rdfs:subclassOf ulo:resource .\r\n" + 
				"    	ulo:student rdfs:subclassOf ulo:user .\r\n" + 
				"    	ulo:teacher rdfs:subclassOf ulo:user .\r\n" + 
				"    	ulo:chatbot rdfs:subclassOf ulo:user .\r\n" + 
				"	}\r\n" + 
				"}";
		String response = sparqlUpdate(query);
	}
	
	public void addResources (JSONArray objects) {
		String resourceQuery = "PREFIX ulo: <http://uni-leipzig.de/tech4comp/ontology/>\r\n" + 
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
				"SELECT DISTINCT ?resourceid WHERE {\r\n" + 
				"    GRAPH <%s> {\r\n" + 
				"    	?b a ulo:Material .\r\n" + 
				"    	?b ulo:id ?resourceid .\r\n" + 
				"  	} \r\n" + 
				"} ";
		
		try {
			JSONArray bindingsArray = getBindings(sparqlQuery(resourceQuery));
			
			HashSet<String> resourceIds = new HashSet<String>();
			for (int i = 0; i < bindingsArray.size(); i++) {
				JSONObject bindingObj = (JSONObject) bindingsArray.get(i);
				resourceIds.add(((JSONObject) bindingObj.get("resourceid")).getAsString("value"));
			}
			
			String query = "PREFIX ulo: <http://uni-leipzig.de/tech4comp/ontology/>\r\n" + 
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
					"    INSERT DATA  {\r\n" + 
					"  	GRAPH <%s> {\r\n";
					
			for (int i = 0 ; i < objects.size() ; i++) {
				JSONObject obj = (JSONObject) objects.get(i);
				String id = obj.getAsString("_id");
				String type = obj.getAsString("type");
				System.out.println("(!!!!: Attempting to add RESOURCE: "+ id+ "type:" +type);

				if (resourceIds.contains(id) || type.equals("post") || type.equals("forum")||type.equals("quiz")||type.equals("file")||type.equals("hyperlink")) {
					if (type.equals("post")) {
						id = id.split("#parent")[0];
					}
					String name = obj.getAsString("name");
					String url = id;
					String courseid = obj.getAsString("courseid");
					
					
					
					query = query
							+ "<" + id + "> a ulo:resource .\r\n"
							+ "<" + id + "> a ulo:" + type + " .\r\n"
							+ "<" + id + "> rdfs:label \"" + name + "\" .\r\n"
							+ "<" + id + "> ulo:url \"" + url + "\" .\r\n"
							+ "<" + courseid + "> ulo:hasResource <" + id + "> .\r\n";
				}
			}

			String response = sparqlUpdate(query + "}\r\n}");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void addUser (JSONArray objects) {
		String query = "PREFIX ulo: <http://uni-leipzig.de/tech4comp/ontology/>\r\n" + 
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
				"    INSERT DATA  {\r\n" + 
				"  	GRAPH <%s> {\r\n";
				
		for (int i = 0 ; i < objects.size() ; i++) {
			JSONObject obj = (JSONObject) objects.get(i);
			String id = "https://moodle.tech4comp.dbis.rwth-aachen.de/user/profile.php?id=" +  obj.getAsString("_id");
			String name = obj.getAsString("name");
			String courseid = obj.getAsString("courseid");
			
			query = query
					+ "<" + id + ">" + " a ulo:user .\r\n"
					+ "<" + id + ">" + " rdfs:label " + "\"" + name + "\"" + " .\r\n"
					+ "<" + courseid + "> ulo:hasUser <" + id + "> .\r\n";
		}

		
		String responseString = sparqlUpdate(query + "}}");
		System.out.println("(!!!!) Attempting to add users with query: "+ query);
	}
	
	public void addInteractions (JSONArray objects) {
		System.out.println("(!!!!): Adding following interactions to Sparql: " + objects.toString());

		//I think this first query gets all the distinct resources in the graph
		String resourceQuery = "PREFIX ulo: <http://uni-leipzig.de/tech4comp/ontology/>\r\n" + 
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
				"    SELECT DISTINCT ?resourceid WHERE {\r\n" + 
				"  		GRAPH <%s> {\r\n" + 
				"    		?b ulo:discussResource ?resourceid .\r\n" + 
				"  		} \r\n" + 
				"    }";
		
		try {
			
			JSONArray bindingsArray = getBindings(sparqlQuery(resourceQuery));
			
			
			HashSet<String> resourceIds = new HashSet<String>();
			for (int i = 0; i < bindingsArray.size(); i++) {
				JSONObject bindingObj = (JSONObject) bindingsArray.get(i);

				// String x = ((JSONObject) bindingObj.get("resourceid")).getAsString("value");
				// fixedSyntaxResourceId = x.substring(0,x.length()-)+"B"+x.substring(3);


				resourceIds.add(((JSONObject) bindingObj.get("resourceid")).getAsString("value"));
			}
			
			String query = "PREFIX ulo: <http://uni-leipzig.de/tech4comp/ontology/>\r\n" + 
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
					"    INSERT DATA  {\r\n" + 
					"  	GRAPH <%s> {\r\n";
			
			for (int i = 0; i < objects.size(); i++) {
				JSONObject obj = (JSONObject) objects.get(i);
				String resourceid = obj.getAsString("objectid");
				//todo: fix the formatting FROM THE LRS, not the triple store of how the resources are described
				//! resourceid format is unequal to the format from the triplestore. resource id has .demod in the string instead of .de/mod... need to fix this.
				String interaction = obj.getAsString("verbShort");
				if (resourceIds.contains(resourceid) || interaction.equals("posted") || interaction.equals("interacted") || interaction.equals("viewed") /*This doesnt help as it adds resources which are not identifiable, the solution should be to repair the string directly form the LRS*/){
					String userid = "https://moodle.tech4comp.dbis.rwth-aachen.de/user/profile.php?id=" + obj.getAsString("userid");
					
					JSONObject information = (JSONObject) obj.get("info");
					
					query = query
							+ "<" + userid + ">" + " ulo:" + interaction + " [\r\n"
							+ "ulo:interactionResource " + "<" + resourceid + ">;\r\n"
							+ "ulo:timestamp " + "\"" + Instant.parse(obj.getAsString("timestamp")).getEpochSecond() + "\";\r\n";
					
					for (Entry<String, Object> entry : information.entrySet()) {
						query = query + ";\r\n ulo:" + entry.getKey() + " \"" + entry.getValue().toString() + "\"";
					}
					
					query = query + "] .\r\n";
				}
			}


			String response = sparqlUpdate(query + "}}");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public JSONArray getUpdates(long since, String courseid) { // Probably some formating error doesnt allow for all the correct results to come through. It seems to be the string is simply built for the courses for the leipzig university, still is strange that some RWTh users come through.
		String query = "PREFIX ulo: <http://uni-leipzig.de/tech4comp/ontology/>\r\n" + 
				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n" + 
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
				"\r\n" + 
				"SELECT DISTINCT ?userid ?username ?resourceid ?resourcename ?resourcetype WHERE {\r\n" + 
				"  GRAPH <%s> {\r\n" + 
				"    <" + courseid + "> ulo:hasResource ?resourceid .\r\n" + 
				"    <" + courseid + "> ulo:hasUser ?userid .\r\n" + 
				"    ?resourceid rdfs:label ?resourcename .\r\n" + 
				"    ?userid rdfs:label ?username .\r\n" + 
				"    ?userid ?interaction ?b .\r\n" + 
				"    ?interaction rdfs:subclassOf ulo:interaction .\r\n" + 
				"  	 ?b ulo:interactionResource ?resourceid .\r\n" + 
				"    ?b ulo:timestamp ?timestamp .\r\n" + 
				"    ?resourceid a ?resourcetype .\r\n" + 
				"    ?resourcetype rdfs:subclassOf ulo:resource .\r\n" + 
				"    FILTER (xsd:integer(?timestamp) > " + since + ").\r\n" + 
				"  }\r\n" + 
				"}";
		
		return getBindings(sparqlQuery(query));
	}
	// public JSONArray getLastInteraction(String courseid, String userid) { // The idea is to get the last interaction of every element
	// 	String query = "PREFIX ulo: <http://uni-leipzig.de/tech4comp/ontology/>\r\n" + 
	// 			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n" + 
	// 			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
	// 			"\r\n" + 
	// 			"SELECT DISTINCT ?username ?resourceid ?resourcename ?resourcetype WHERE {\r\n" + 
	// 			"  GRAPH <%s> {\r\n" + 
	// 			"    <" + courseid + "> ulo:hasResource ?resourceid .\r\n" + 
	// 			"    <" + courseid + "> ulo:hasUser <" + userid + "> .\r\n" + 
	// 			"    ?resourceid rdfs:label ?resourcename .\r\n" + 
	// 			"    <" + userid + "> rdfs:label ?username .\r\n" + 
	// 			"    <" + userid + "> ?interaction ?b .\r\n" + 
	// 			"    ?interaction rdfs:subclassOf ulo:interaction .\r\n" + 
	// 			"  	 ?b ulo:interactionResource ?resourceid .\r\n" + 
	// 			"    ?b ulo:timestamp ?timestamp .\r\n" + 
	// 			"    ?resourceid a ?resourcetype .\r\n" + 
	// 			"    ?resourcetype rdfs:subclassOf ulo:resource .\r\n" + 
	// 			"    FILTER (xsd:integer(?timestamp) > " + since + ").\r\n" + 
	// 			"  }\r\n" + 
	// 			"}";
		
	// 	return getBindings(sparqlQuery(query));
	// }
	
	public ArrayList<String> getInteractions (String userid, String resourceid) {
		ArrayList<String> interactions = new ArrayList<String>();
		String query = "PREFIX ulo: <http://uni-leipzig.de/tech4comp/ontology/>\r\n" + 
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
				"\r\n" + 
				"SELECT DISTINCT ?interaction WHERE {\r\n" + 
				"  GRAPH <%s> {\r\n" + 
				"    <https://moodle.tech4comp.dbis.rwth-aachen.de/user/profile.php?id=" + userid + "> ?interaction ?b .\r\n" + 
				"    ?interaction rdfs:subclassOf ulo:interaction .\r\n" + 
				"  	?b ulo:interactionResource <" + resourceid + "> .\r\n" + 
				"  }\r\n" + 
				"}";
		JSONArray bindings = getBindings(sparqlQuery(query));
		for (int i = 0; i < bindings.size(); i++) {
			JSONObject obj = (JSONObject) bindings.get(i);
			interactions.add(((JSONObject) obj.get("interaction")).getAsString("value"));
		}
		//DEBUG System.out.println("--DEBUG: REturning interaction from spaqrl for user and resource with query: "+query+ interactions);
		return interactions;
	}
	
	public double getBestGrade(String userid, String resourceid) {
		double result = 0;
		String query = "PREFIX ulo: <http://uni-leipzig.de/tech4comp/ontology/>\r\n" + 
				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n" + 
				"\r\n" + 
				"SELECT ?score WHERE {\r\n" + 
				"  GRAPH <%s> {\r\n" + 
				"    <https://moodle.tech4comp.dbis.rwth-aachen.de/user/profile.php?id=" + userid + "> ulo:completed ?b .\r\n" + 
				"    ?b ulo:interactionResource <" + resourceid + "> .\r\n" + 
				"  	?b ulo:score ?score .\r\n" + 
				"  }\r\n" + 
				"}ORDER BY DESC(xsd:double(?score)) LIMIT 1";
		
		JSONArray bindings = getBindings(sparqlQuery(query));
		if (!bindings.isEmpty()) {
			JSONObject obj = (JSONObject) bindings.get(0);
			String resultTemp = ((JSONObject) obj.get("score")).getAsString("value");
			System.out.println(resultTemp);
			result = Double.parseDouble(resultTemp);
		}
		return result;
	} 

	public double getCognitiveLoad(String resourceid) {
		double result =  0; 
		String query = "PREFIX ulo: <http://uni-leipzig.de/tech4comp/ontology/>\r\n" + 
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n" + 
			"\r\n" + 
			"SELECT DISTINCT ?cognitiveLoad WHERE {\r\n" + 
			"  GRAPH <https://triplestore.tech4comp.dbis.rwth-aachen.de/LMSData/data> {\r\n" + 
			"    ?b a ulo:resource .\r\n" + 
			"  	?b ulo:cognitiveLoad ?cognitiveLoad .\r\n" +
			"  	?b ulo:url "+'"'+resourceid+'"'+".\r\n" +  
			"  }\r\n" + 
			"}";
		System.out.println("Attempting Cognitive Load query: "+query+ "\r\n and endpoint: "+ endpoint);
		System.out.println(sparqlQuery(query));
		JSONArray bindings = getBindings(sparqlQuery(query));
		if (!bindings.isEmpty()) {
			JSONObject obj = (JSONObject) bindings.get(0); 
			// result = ((JSONObject) obj.get("cognitiveLoad")).getAsNumber("value").doubleValue();
			String result2 = ((JSONObject) obj.get("cognitiveLoad")).getAsString("value");
			System.out.println(result2);
			result = Double.parseDouble(result2);

		}
		else{
			System.out.println("The return from sparql was empty searching for Cognitiveload");
		}
		return result;
	}
	
	// !!!Should not be used anymore, since it collects all themes, not only those of a course!!!
	public JSONArray getThemes() {
		String query = "PREFIX ulo: <http://uni-leipzig.de/tech4comp/ontology/>\r\n" + 
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
				"	\r\n" + 
				"    SELECT ?themeid ?name WHERE {\r\n" + 
				"  		GRAPH <%s> {\r\n" + 
				"  			?themeid a ulo:theme .  \r\n" + 
				"  			?themeid rdfs:label ?name .  \r\n" + 
				"		}\r\n" + 
				"    }";
		
		return getBindings(sparqlQuery(query));
	}

	public JSONArray getThemes(String id) {
		String query = "PREFIX ulo: <http://uni-leipzig.de/tech4comp/ontology/>\r\n" +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" +
				"	\r\n" +
				"    SELECT ?themeid ?name WHERE {\r\n" +
				"  		GRAPH <%s> {\r\n" +
				"           <" + id + "> ulo:hasTheme ?themeid .  \r\n" +
				"  			?themeid rdfs:label ?name .  \r\n" +
				"		}\r\n" +
				"    }";

		return getBindings(sparqlQuery(query));
	}

	@Deprecated
	public JSONArray getThemeStructure() {
		String query = "PREFIX ulo: <http://uni-leipzig.de/tech4comp/ontology/>\r\n" + 
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
				"	\r\n" + 
				"    SELECT ?supertheme ?subtheme WHERE {\r\n" + 
				"  		GRAPH <%s> {\r\n" + 
				"  			?supertheme ulo:relatedTo ?subtheme .  \r\n" + 
				"		}\r\n" + 
				"    }";
		
		return getBindings(sparqlQuery(query));
	}

	public JSONArray getThemeStructure(String id) {
		String query = "PREFIX ulo: <http://uni-leipzig.de/tech4comp/ontology/>\r\n" +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" +
				"	\r\n" +
				"    SELECT ?supertheme ?subtheme WHERE {\r\n" +
				"  		GRAPH <%s> {\r\n" +
							"<" + id + "> ulo:hasTheme ?supertheme . \r\n" +
							"<" + id + "> ulo:hasTheme ?subtheme . \r\n" +
				"  			?supertheme ulo:relatedTo ?subtheme .  \r\n" +
				"		}\r\n" +
				"    }";

		return getBindings(sparqlQuery(query));
	}

	@Deprecated
	public JSONArray getThemesInfo() {
		String query = "PREFIX ulo: <http://uni-leipzig.de/tech4comp/ontology/>\r\n" + 
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
				"    SELECT ?themeid ?resourceid ?infoType ?infoVal WHERE {\r\n" +
				"  		GRAPH <%s> {\r\n" + 
				"    		?themeid ulo:discusses ?s1 .\r\n" + 
				"  			?s1 ulo:discussResource ?resourceid .\r\n" + 
				"    		?s1 ?infoType ?infoVal .\r\n" +
				"  		} \r\n" + 
				"    } ";
		
		return getBindings(sparqlQuery(query));
	}
	public JSONArray getThemesInfo(String id) {
		String query = "PREFIX ulo: <http://uni-leipzig.de/tech4comp/ontology/>\r\n" +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" +
				"    SELECT ?themeid ?resourceid ?infoType ?infoVal WHERE {\r\n" +
				"  		GRAPH <%s> {\r\n" +
							"<" + id + "> ulo:hasTheme ?themeid .\r\n" +
				"    		?themeid ulo:discusses ?s1 .\r\n" +
				"  			?s1 ulo:discussResource ?resourceid .\r\n" +
				"    		?s1 ?infoType ?infoVal .\r\n" +
				"  		} \r\n" +
				"    } ";

		return getBindings(sparqlQuery(query));
	}

	public JSONArray getResourceInfo(String resId) {
		String query = "PREFIX ulo: <http://uni-leipzig.de/tech4comp/ontology/>\r\n" +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" +
				"    SELECT ?resourceName ?resourceType WHERE {\r\n" +
				"  		GRAPH <%s> {\r\n" +
							"<" + resId + "> rdfs:label ?resourceName .\r\n" +
							"<" + resId + "> a ?resourceType .\r\n" +
				"  		} \r\n" +
				"    } ";

		return getBindings(sparqlQuery(query));
	}
	
	public String sparqlQuery(String query) {
		try {
			URL url = new URL(endpoint);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/sparql-query");
			conn.setRequestProperty("Accept", "application/json");
			conn.setDoOutput(true);
			
			try(OutputStream os = conn.getOutputStream()) {
			    byte[] input = String.format(query, graphurl).getBytes("utf-8");
			    os.write(input, 0, input.length);			
			}
			StringBuilder response = new StringBuilder();
			try(BufferedReader br = new BufferedReader(
					  new InputStreamReader(conn.getInputStream(), "utf-8"))) {
					    String responseLine = null;
					    while ((responseLine = br.readLine()) != null) {
					        response.append(responseLine.trim());
					    }
					}
			return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "SPARQL connection failed";
		}
	}
	
	public String sparqlUpdate(String query) {
		try {
			URL url = new URL(endpoint);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			conn.setRequestProperty("Accept", "application/json");
			conn.setDoOutput(true);
			
			try(OutputStream os = conn.getOutputStream()) {
			    byte[] input = String.format("update=" + query, graphurl).getBytes("utf-8");
			    os.write(input, 0, input.length);			
			}
			StringBuilder response = new StringBuilder();
			try(BufferedReader br = new BufferedReader(
					  new InputStreamReader(conn.getInputStream(), "utf-8"))) {
					    String responseLine = null;
					    while ((responseLine = br.readLine()) != null) {
					        response.append(responseLine.trim());
					    }
					}
			return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "SPARQL connection failed";
		}
	}
	
	private JSONArray getBindings(String queryResult) {
		JSONArray result = new JSONArray();
		try {
			JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
			JSONObject responseObj = (JSONObject) parser.parse(queryResult.toString());
			JSONObject resultsObj = (JSONObject) responseObj.get("results");
			result = (JSONArray) resultsObj.get("bindings");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
