package i5.las2peer.services.mentoringCockpitService;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.Base64;
import java.util.HashMap;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import i5.las2peer.api.Context;
import i5.las2peer.api.ManualDeployment;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.services.mentoringCockpitService.Model.Course;
import i5.las2peer.services.mentoringCockpitService.Model.MoodleCourse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

@Api
@SwaggerDefinition(
		info = @Info(
				title = "Mentoring Cockpit Service",
				version = "1.0",
				description = "A service for managing LRS data and access rights of the Mentoring Cockpit",
				contact = @Contact(
						name = "Philipp Roytburg",
						email = "philipp.roytburg@rwth-aachen.de")))


/**
 * 
 * This service is for managing data from an LRS and access rights for a tutor in the Mentoring Cockpit. It gets identification subs
 * of a tutor and requests the data for the tutor from the LRS.
 * 
 */
@ManualDeployment
@ServicePath("/mentoring")
public class MentoringCockpitService extends RESTService {

	private String lrsDomain;
	private String lrsAuth;
	private String mysqlUser;
	private String mysqlPassword;
	private String mysqlHost;
	private String mysqlPort;
	private String mysqlDatabase;
	public String triplestoreDomain = "https://triplestore.tech4comp.dbis.rwth-aachen.de/LMSData";
	private static String userEmail;
	private String lrsClientURL;
	public HashMap<String, Course> courses;
	
	
	/**
	 * 
	 * Constructor of the Service. Loads the database values from a property file.
	 * 
	 */
	public MentoringCockpitService() {
		setFieldValues();
		userEmail = "askabot@fakemail.de"; //TODO: remove this
		courses = new HashMap<String, Course>();
		createCourses();
	}

    /**
     * A function that is called by the Mentoring Cockpit to get students extended statistics in a course.
     *
     * @param tutorSub an identification string of the tutor
     *
     * @param courseEncoded a hex encoded string of the course URL
     *
     * @param studentSub an identification string of the student
     *
     * @return an application octet stream, error message or unauthorized message
     *
     */
    @GET
    @Path("/mwb/{tutorsub}/{courseEncoded}/{studentsub}/extendedstatistics")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @ApiResponses(
            value = { @ApiResponse(
                    code = HttpURLConnection.HTTP_OK,
                    message = "Connection works") })
    public Response getMWBExtendedStatistics(@PathParam("tutorsub") String tutorSub, @PathParam("courseEncoded") String courseEncoded, @PathParam("studentsub") String studentSub) {
    	// build sample graph
		double[] yData = new double[] { 2.0, 1.0, 0.0 };

		// Create Chart
		XYChart chart = new XYChart(500, 400);
		chart.setTitle("Sample Chart for " + studentSub);
		chart.setXAxisTitle("X");
		chart.setXAxisTitle("Y");
		XYSeries series = chart.addSeries("y(x)", null, yData);
		series.setMarker(SeriesMarkers.CIRCLE);

		// build png image
		StreamingOutput output = new StreamingOutput() {
			@Override
			public void write(OutputStream out) throws IOException {
				BitmapEncoder.saveBitmap(chart, out, BitmapEncoder.BitmapFormat.PNG);
			}
		};

		// filename
		String filename = "MWB-" + studentSub + ".png";

		return Response.ok(output).header("Content-Disposition", "attachment; filename=\"" + filename + "\"").build();
	}
	
	/**
	 * A function that is called by the Mentoring Cockpit to get a student list of a course. 
	 *
	 * @param sub an identification string of the tutor
	 * 
	 * @param courseEncoded a hex encoded string of the course URL
	 * 
	 * @return a response message with the data, error message or unauthorized message
	 * 
	 */
	@GET
	@Path("/{sub}/{courseEncoded}/students")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Connection works") })
	public Response getStudents(@PathParam("sub") String sub, @PathParam("courseEncoded") String courseEncoded) {
		String courseList = getCourseListFromMysql(sub).replace("\\", "");
		String course = convertHexToString(courseEncoded);
		if(courseList.equals("Error")) {
			return Response.status(500).entity("An error occured with the MySQL database").build();
		} else if(courseList.contains(course)) {
			String response = getStudentsByCourse(course);
			return Response.ok().entity(response).build();
		} else {
			return Response.status(401).entity("Unauthorized").build();
		}
	}
	
	/**
	 * A function that is called by the Mentoring Cockpit to get students results in a course. 
	 *
	 * @param sub an identification string of the tutor
	 * 
	 * @param courseEncoded a hex encoded string of the course URL
	 * 
	 * @return a response message with the data, error message or unauthorized message
	 * 
	 */
	@GET
	@Path("/{sub}/{courseEncoded}/results")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Connection works") })
	public Response getResults(@PathParam("sub") String sub, @PathParam("courseEncoded") String courseEncoded) {
		String courseList = getCourseListFromMysql(sub).replace("\\", "");
		String course = convertHexToString(courseEncoded);
		if(courseList.equals("Error")) {
			return Response.status(500).entity("An error occured with the MySQL database").build();
		} else if(courseList.contains(course)) {
			String response = getResultsByCourse(course);
			return Response.ok().entity(response).build();
		} else {
			return Response.status(401).entity("Unauthorized").build();
		}
	}
	
	/**
	 * A function that is called by the Mentoring Cockpit to get a list of courses a tutor can see. 
	 *
	 * @param sub an identification string of the tutor
	 * 
	 * @return a response message with the data or error message
	 * 
	 */
	@GET
	@Path("/{sub}/courseList")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Connection works") })
	public Response getCourseList(@PathParam("sub") String sub, @HeaderParam("email") String email) {
		userEmail = email;
		String courseList = getCourseListFromMysql(sub);
		if(courseList.equals("Error")) return Response.status(500).entity("An error occured with the Mysql database").build();
		else return Response.ok().entity(courseList).build();
	}

	/**
	 * A function that is called by the Mentoring Cockpit to get a list of sensor data a tutor can see. 
	 *
	 * @param sub an identification string of the tutor
	 *
	 * @param courseEncoded a hex encoded string of the course URL
	 * 
	 * @return a response message with the data or error message
	 * 
	 */
	@GET
	@Path("/{sub}/{courseEncoded}/sensor")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Connection works") })
	public Response getSensorDataList(@PathParam("sub") String sub, @PathParam("courseEncoded") String courseEncoded) {
		String courseList = getCourseListFromMysql(sub).replace("\\", "");
		String course = convertHexToString(courseEncoded);
		if(courseList.equals("Error")) {
			return Response.status(500).entity("An error occured with the MySQL database").build();
		} else if(courseList.contains(course)) {
			String response = getSensorData();
			return Response.ok().entity(response).build();
		} else {
			return Response.status(401).entity("Unauthorized").build();
		}
	}


	
	/**
	 * A function that gets all the courses a tutor can see from a MySQL database. 
	 *
	 * @param sub an identification string of the tutor
	 * 
	 * @return an JSONArray converted to a String, that contains the course name and URL
	 * 
	 */
	private String getCourseListFromMysql(String sub) {
		try{
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection con = DriverManager.getConnection("jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + mysqlDatabase,
					mysqlUser, mysqlPassword);
			
			JSONArray courseArr = new JSONArray();
			
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("select COURSELINK, COURSENAME from ACCESS where SUB = '" + sub + "'");
			while(rs.next()) {
				JSONObject course = new JSONObject();
				course.put("name", rs.getString("COURSENAME"));
				course.put("link", rs.getString("COURSELINK"));
				courseArr.add(course);
			}
			
			con.close();
			return courseArr.toJSONString();
		} catch(Exception e) {
			System.out.println(e);
			return "Error";
		}

	}
	
	
	/**
	 * A function that converts a hexadecimal to a String. 
	 * 
	 * @param hex the hexadecimal as a String
	 * 
	 * @return the decoded String
	 * 
	 */
	private String convertHexToString(String hex){

		StringBuilder sb = new StringBuilder();
		StringBuilder temp = new StringBuilder();
		
		for(int i=0; i<hex.length()-1; i+=2 ){
			String output = hex.substring(i, (i + 2));
			int decimal = Integer.parseInt(output, 16);
			//convert the decimal to character
			sb.append((char)decimal);
			
			temp.append(decimal);
		}
		
		return sb.toString();
	}
	
	
	/**
	 * A function that gets list of students in a course 
	 * 
	 * @param course the URL of the course
	 * 
	 * @return JSONArray converted to a String, containing the data
	 * 
	 */
	private String getStudentsByCourse(String course) {
		JSONObject typeObj = new JSONObject();
		typeObj.put("statement.object.definition.type", course);
		
		JSONObject matchObj = new JSONObject();
		matchObj.put("$match", typeObj);
		
		JSONObject nameObj = new JSONObject();
		nameObj.put("$first","$statement.actor.name");

		JSONObject averageScoreObj = new JSONObject();
		averageScoreObj.put("$avg", "$statement.result.score.scaled");
		
		JSONObject actorObj = new JSONObject();
		actorObj.put("name", nameObj);
		actorObj.put("_id", "$statement.actor.mbox");
		actorObj.put("averageScore", averageScoreObj);
		
		JSONObject groupObj = new JSONObject();
		groupObj.put("$group", actorObj);

		JSONObject avgSort = new JSONObject();
		avgSort.put("averageScore", 1);

		JSONObject sortObj = new JSONObject();
		sortObj.put("$sort", avgSort);

		JSONObject project = new JSONObject();
		project.put("_id", 1);
		project.put("name", 1);
		project.put("averageScore", 1);

		JSONObject projectObj = new JSONObject();
		projectObj.put("$project", project);
		
		JSONArray arr = new JSONArray();
		arr.add(matchObj);
		arr.add(groupObj);
		arr.add(sortObj);
		arr.add(projectObj);
		
		StringBuilder sb = new StringBuilder();
		for (byte b : arr.toString().getBytes()) {
			sb.append("%" + String.format("%02X", b));
		}
		
		return LRSconnect(sb.toString());
	}
	
	/**
	 * A function that gets results of students in a course 
	 * 
	 * @param course the URL of the course
	 * 
	 * @return JSONArray converted to a String, containing the data
	 * 
	 */
	private String getResultsByCourse(String course) {
		//match block
		JSONObject matchObj = new JSONObject();
		matchObj.put("statement.object.definition.type", course);
		JSONObject match = new JSONObject();
		match.put("$match", matchObj);
		
		//project block
		JSONObject projectObj = new JSONObject();
		projectObj.put("_id", "$statement.actor.mbox");
		projectObj.put("statement.object", 1);
		projectObj.put("statement.result.score.scaled", 1);
		projectObj.put("statement.timestamp", 1);
		projectObj.put("statement.result.response", 1);
		JSONObject project = new JSONObject();
		project.put("$project", projectObj);
		
		//sort block
		JSONObject sortObj = new JSONObject();
		sortObj.put("statement.timestamp", 1);
		JSONObject sort = new JSONObject();
		sort.put("$sort", sortObj);
		
		//group block
		JSONObject groupObj = new JSONObject();
		groupObj.put("_id", "$_id");
		//averageScore block
		JSONObject averageScoreObj = new JSONObject();
		averageScoreObj.put("$avg", "$statement.result.score.scaled");
		groupObj.put("averageScore", averageScoreObj);
		//results block
		JSONObject pushObj = new JSONObject();
		pushObj.put("objectId", "$statement.object.id");
		pushObj.put("name", "$statement.object.definition.name.en-US");
		pushObj.put("score", "$statement.result.score.scaled");
		pushObj.put("description", "$statement.object.definition.description.en-US");
		pushObj.put("feedback", "$statement.result.response");

		JSONObject push = new JSONObject();
		push.put("$push", pushObj);
		groupObj.put("results", push);
		JSONObject group = new JSONObject();
		group.put("$group", groupObj);
		
		JSONArray arr = new JSONArray();
		arr.add(match);
		arr.add(project);
		arr.add(sort);
		arr.add(group);
		
		StringBuilder sb = new StringBuilder();
		for (byte b : arr.toString().getBytes()) {
			sb.append("%" + String.format("%02X", b));
		}
		
		return LRSconnect(sb.toString());
	}

	/**
	 * A function that gets sensor data 
	 * 
	 * @return JSONArray converted to a String, containing the data
	 * 
	 */
	private String getSensorData() {
		JSONObject match = new JSONObject();
		match.put("statement.verb.id", "http://example.com/xapi/performed");
		
		JSONObject matchObj = new JSONObject();
		matchObj.put("$match", match);
		

		JSONObject project = new JSONObject();
		project.put("_id", "$statement.actor.mbox");
		project.put("objectDesc", "$statement.object.definition.description.en-US");
		project.put("objectName", "$statement.object.definition.name.en-US");


		JSONObject projectObj = new JSONObject();
		projectObj.put("$project", project);

		JSONObject pushObj = new JSONObject();
		pushObj.put("objectDesc", "$objectDesc");
		pushObj.put("objectName", "$objectName");

		JSONObject actions = new JSONObject();
		actions.put("$push", pushObj);

		JSONObject group = new JSONObject();
		group.put("_id", "$_id");
		group.put("actions", actions);

		JSONObject groupObj = new JSONObject();
		groupObj.put("$group", group);

		
		JSONArray arr = new JSONArray();
		arr.add(matchObj);
		arr.add(projectObj);
		arr.add(groupObj);
		
		StringBuilder sb = new StringBuilder();
		for (byte b : arr.toString().getBytes()) {
			sb.append("%" + String.format("%02X", b));
		}
		
		return LRSconnect(sb.toString());
	}

	
	/**
	 * A function connects to the LRS and requests data
	 * 
	 * @param pipeline the request encoded in a byte stream
	 * 
	 * @return JSONArray converted to a String, containing the data
	 * 
	 */
	public String LRSconnect(String pipeline)  {
		StringBuffer response = new StringBuffer();
		String clientKey;
		String clientSecret;
		Object clientId = null;
		try {
			clientId = searchIfClientExists();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//If Client exists in LRS
		if(!(clientId).equals("noClientExists")) {
			clientKey = (String) ((JSONObject) clientId).get("basic_key");
			clientSecret = (String) ((JSONObject) clientId).get("basic_secret");
			String auth = Base64.getEncoder().encodeToString((clientKey + ":" + clientSecret).getBytes());

			try {
				URL url = new URL(lrsDomain + "pipeline=" + pipeline);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
				conn.setRequestProperty("Authorization","Basic " + auth);
				//System.out.println("DEBUG --- URL: " + conn.getURL().toString());

				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

				String inputLine;

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				conn.disconnect();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} 
			return response.toString();
		}
		else {
			return String.valueOf(Response.status(500).entity("Client does not exist in LRS").build());
		}
	}

	/*
	* Checks if LRS client of the respective moodle token is available or not
	* */

	private Object searchIfClientExists() throws IOException, ParseException {
		String moodleToken = "";
		URL url = null;
		try{
			Connection con = connectToDatabase();
			Statement stmt = con.createStatement();
			//System.out.println("DEBUG --- Email: " + userEmail);
			ResultSet rs = stmt.executeQuery("select MOODLE_TOKEN from moodle_lrs_mapping where EMAIL = '" + userEmail + "'");
			while(rs.next()) {
				moodleToken = rs.getString("moodle_token");
				//System.out.println("DEBUG --- Token: " + moodleToken);
			}
			con.close();

			try {
				String clientURL = lrsClientURL;
				url = new URL(clientURL);
				HttpURLConnection conn = null;
				conn = (HttpURLConnection) url.openConnection();
				conn.setDoOutput(true);
				conn.setDoInput(true);
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
				conn.setRequestProperty("X-Experience-API-Version","1.0.3");
				conn.setRequestProperty("Authorization", lrsAuth);
				conn.setRequestProperty("Cache-Control", "no-cache");
				conn.setUseCaches(false);

				InputStream is = conn.getInputStream();
				BufferedReader rd = new BufferedReader(new InputStreamReader(is));
				String line;
				StringBuilder response = new StringBuilder();
				while((line = rd.readLine()) != null) {
					response.append(line);
				}
				Object obj= JSONValue.parse(response.toString());

				for(int i = 0 ; i < ((JSONArray ) obj).size(); i ++) {
					JSONObject client = (JSONObject) ((JSONArray) obj).get(i);
					if(client.get("title").equals(moodleToken)) {
						return client.get("api");
					}
				}
			}  catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch(Exception e) {
			System.out.println(e);
			return "Error";
		}

		return "noClientExists";
	}

	private Connection connectToDatabase(){
		Connection con = null;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + mysqlDatabase,
					mysqlUser, mysqlPassword);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return con;
	}
	
	public void createCourses() {
		try{
			Class.forName("com.mysql.cj.jdbc.Driver");
			//System.out.println("DEBUG --- Connection: " + mysqlHost + ":" + mysqlPort + "/" + mysqlDatabase);
			Connection con = DriverManager.getConnection("jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + mysqlDatabase,
					mysqlUser, mysqlPassword);
			
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("select COURSELINK from ACCESS");
			while(rs.next()) {
				String link = rs.getString("COURSELINK");
				String courseid = link.split("id=")[1];
				MoodleCourse course = new MoodleCourse(courseid, link, this);
				courses.put(courseid, course);
			}
			con.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	
	@Override
	protected void initResources() {
		getResourceConfig().register(Suggestions.class);
	}
	
	@Api(
			value = "Suggestion resource")
	@SwaggerDefinition(
			info = @Info(
					title = "Mentoring Cockpit Service",
					version = "1.0.0",
					description = "",
					termsOfService = "",
					contact = @Contact(
							name = "Leonardo da Matta",
							url = "",
							email = "leonardo.matta@rwth-aachen.de"),
					license = @License(
							name = "",
							url = "")))
	@Path("/suggestions")
	public static class Suggestions {
		MentoringCockpitService service = (MentoringCockpitService) Context.get().getService();
		
		/**
	     * A function that is called by a chatbot to generate a suggestion for a user.
	     *
	     * @body Request body of the chatbot
	     *
	     */
	    @POST
	    @Path("/getSuggestion")
		@Consumes(MediaType.TEXT_PLAIN)
	    @Produces(MediaType.APPLICATION_JSON)
	    @ApiOperation(
				value = "Get Suggestion",
				notes = "Returns a resource suggestion for the given course and user.")
	    @ApiResponses(
	            value = { @ApiResponse(
	                    code = HttpURLConnection.HTTP_OK,
	                    message = "Connection works") })
	    public Response getSuggestion(String body) {
	    	JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
	    	JSONObject returnObj = new JSONObject();
	    	try {
	    		JSONObject bodyObj = (JSONObject) parser.parse(body);
	    		String userid = bodyObj.getAsString("user");
	    		String courseid = bodyObj.getAsString("courseid");
	    		returnObj.put("text", this.service.courses.get(courseid).getSuggestion(userid, courseid));
	    		returnObj.put("closeContext", "true");
	    		return Response.status(200).entity(returnObj).build();
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    		returnObj.put("text", "Error");
	    		return Response.status(400).entity(returnObj).build();
	    	}
	    	
		}
	    
	    /**
	     * A function that is called by a chatbot to generate a suggestion for a user based on a specific Theme.
	     *
	     * @body Request body of the chatbot
	     *
	     */
	    @POST
	    @Path("/getSuggestionByTheme")
		@Consumes(MediaType.TEXT_PLAIN)
	    @Produces(MediaType.APPLICATION_JSON)
	    @ApiOperation(
				value = "Get Suggestion",
				notes = "Returns a list of resources related to the given theme.")
	    @ApiResponses(
	            value = { @ApiResponse(
	                    code = HttpURLConnection.HTTP_OK,
	                    message = "Connection works") })
	    public Response getSuggestionByTheme(String body) {
	    	JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
	    	JSONObject returnObj = new JSONObject();
	    	try {
	    		JSONObject bodyObj = (JSONObject) parser.parse(body);
	    		String themeid = bodyObj.getAsString("intent");
	    		String courseid = bodyObj.getAsString("courseid");
	    		returnObj.put("text", this.service.courses.get(courseid).getThemeSuggestions(themeid, courseid));
	    		returnObj.put("closeContext", "true");
	    		return Response.status(200).entity(returnObj).build();
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    		returnObj.put("text", "Error");
	    		return Response.status(400).entity(returnObj).build();
	    	}
	    	
		}
	}
	
	
}
