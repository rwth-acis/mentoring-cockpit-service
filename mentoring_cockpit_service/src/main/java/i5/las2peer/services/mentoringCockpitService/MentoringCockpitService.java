package i5.las2peer.services.mentoringCockpitService;

import java.beans.ConstructorProperties;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.net.URLEncoder;

import javax.ws.rs.*;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.Response.ResponseBuilder;

import javax.ws.rs.core.UriBuilder;
import i5.las2peer.services.mentoringCockpitService.SPARQLConnection.SPARQLConnection;
import i5.las2peer.services.mentoringCockpitService.Suggestion.*;


import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.w3c.dom.Text;

import i5.las2peer.api.Context;
import i5.las2peer.api.ManualDeployment;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.services.mentoringCockpitService.Model.Course;
import i5.las2peer.services.mentoringCockpitService.Model.MoodleCourse;
import i5.las2peer.api.security.Agent;
import i5.las2peer.api.security.UserAgent;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
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
 * This service is for managing data from an LRS and access rights for a tutor in the Mentoring Cockpit. It gets
 * identification subs of a tutor and requests the data for the tutor from the LRS.
 * 
 */
@ManualDeployment
@ServicePath("/mentoring")
public class MentoringCockpitService extends RESTService {

	private static String UPLOAD_FOLDER = "/opt/feedback/";
	private List<String> feedbackAccessAllowed = new ArrayList<>();

	// LRS
	private String lrsDomain;
	private String lrsStatementURL;
	private String lrsAuth;
	private String lrsClientURL;
	private String feedbackLRSAuth;

	// MySQL DB
	private String mysqlUser;
	private String mysqlPassword;
	private String mysqlHost;
	private String mysqlPort;
	private String mysqlDatabase;

	// other URLs
	public String sparqlUrl;
	private String eRecUrl;
	private String limeUrl;

	private String userEmail;
	public HashMap<String, Course> courses;
	private String evalMethod;
	private SuggestionEvaluator suggestEvaluator;

	//temporarily solution to issue #6
	private String moodleToken;


	/**
	 * 
	 * Constructor of the Service. Loads the database values from a property file.
	 * 
	 */
	public MentoringCockpitService() {
		// add entries to feedback access allowed list
		feedbackAccessAllowed.add("bja-tud");
		feedbackAccessAllowed.add("neumann");
		// set field values
		setFieldValues();
		if (this.evalMethod.equals("ERec")) {
			this.suggestEvaluator = new ERecSuggestionEvaluator(0, 1);
		} else {
			this.suggestEvaluator = new MoodleSuggestionEvaluator(0, 1);
		}
		this.lrsStatementURL = this.lrsDomain + "/api/statements/aggregate?";
		this.lrsClientURL = this.lrsDomain + "/api/v2/client/";
		courses = new HashMap<String, Course>();

		System.out.println("Creating course...");
		createCourses();
	}
	
	@Override
	protected void initResources() {
		getResourceConfig().register(Suggestions.class);
		getResourceConfig().register(this);
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
	public Response getMWBExtendedStatistics(@PathParam("tutorsub") String tutorSub,
			@PathParam("courseEncoded") String courseEncoded, @PathParam("studentsub") String studentSub) {
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
		if (courseList.equals("Error")) {
			return Response.status(500).entity("An error occured with the MySQL database").build();
		} else if (courseList.contains(course)) {
			String response = getStudentsByCourse(course);
			return Response.ok().entity(response).build();
		} else {
			return Response.status(401).entity("Unauthorized").build();
		}
	}

	/**
	 * A function that is called by the Mentoring Cockpit to get students' results in a course.
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
		if (courseList.equals("Error")) {
			return Response.status(500).entity("An error occured with the MySQL database").build();
		} else if (courseList.contains(course)) {
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
		if (courseList.equals("Error"))
			return Response.status(500).entity("An error occured with the Mysql database").build();
		else
			return Response.ok().entity(courseList).build();
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
		if (courseList.equals("Error")) {
			return Response.status(500).entity("An error occured with the MySQL database").build();
		} else if (courseList.contains(course)) {
			String response = getSensorData();
			return Response.ok().entity(response).build();
		} else {
			return Response.status(401).entity("Unauthorized").build();
		}
	}

	/**
     * A function that is called by a chatbot to generate a suggestion for a user.
     *
     * @param body Request body of the chatbot
     *
     */
    @GET
    @Path("/assignBots")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Connection works") })
	public Response assignBots(String body) {
		try{
			JSONObject courseMap = new JSONObject();
			
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection con = DriverManager.getConnection("jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + mysqlDatabase + "?useSSL=false",
					mysqlUser, mysqlPassword);
			
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("select * from ACCESS");
			while(rs.next()) {
				String botName = rs.getString("SUB");
				String link = rs.getString("COURSELINK");
				String courseid = link.split("id=")[1];
				courseMap.put(botName, courseid);
			}
			con.close();
			JSONObject obj = new JSONObject();
			obj.put("courseMap", courseMap);
			
			System.out.println("\u001B[33mDebug --- CourseMap: " + obj.toString() + "\u001B[0m");
			Context.get().monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_1, obj.toString());
			return Response.status(200).entity("Bots assigned.").build();
			
		} catch(Exception e) {
			System.out.println(e);
			return Response.status(400).entity("Failed.").build();
		}
	}
	
	@GET
	@Path("/test/{email}")
	@Produces("application/pdf")
	@RolesAllowed("authenticated")
	public Response getTestFeedback(@PathParam("email") String email) {
		// authentication
		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof UserAgent) {
			UserAgent userAgent = (UserAgent) agent;
			String name = userAgent.getLoginName();
			if (feedbackAccessAllowed.contains(name)) {
				File file = new File(UPLOAD_FOLDER + email + ".pdf");
				if (!file.exists()) {
					Response.ResponseBuilder response = Response.status(Response.Status.NOT_FOUND);
					return response.build();
				}

				Response.ResponseBuilder response = Response.ok(file);
				response.header("Content-Disposition", "attachment; filename=\"" + email + ".pdf\"");
				return response.build();
			} else {
				return Response.status(Response.Status.FORBIDDEN).entity("Access denied. Wrong user.").build();
			}
		} else {
			return Response.status(Response.Status.FORBIDDEN).entity("Anonymous access denied").build();
		}
	}

	@POST
	@Path("/test")
	@Produces("text/csv")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@RolesAllowed("authenticated")
	public Response uploadPdfFile(FormDataMultiPart multipartFormDataInput) {
		// authentication
		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof UserAgent) {
			UserAgent userAgent = (UserAgent) agent;
			String name = userAgent.getLoginName();
			if (feedbackAccessAllowed.contains(name)) {
				// local variables
				MultivaluedMap<String, String> multivaluedMap = null;
				String fileName = null;
				InputStream inputStream = null;
				String uploadFilePath = null;

				try {
					Map<String, List<FormDataBodyPart>> map = multipartFormDataInput.getFields();
					List<FormDataBodyPart> lstInputPart = map.get("uploadedFile");

					for (FormDataBodyPart inputPart : lstInputPart) {

						// get filename to be uploaded
						multivaluedMap = inputPart.getHeaders();
						fileName = getFileName(multivaluedMap);

						if (null != fileName && !"".equalsIgnoreCase(fileName)) {

							// write & upload file to UPLOAD_FILE_SERVER

							inputStream = inputPart.getEntityAs(InputStream.class);
							uploadFilePath = writeToFileServer(inputStream, fileName);

							// close the stream
							inputStream.close();
						}
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
				} finally {
					// release resources, if any
				}
				return Response.ok("File uploaded successfully at " + uploadFilePath).build();
			} else {
				return Response.status(Response.Status.FORBIDDEN).entity("Access denied. Wrong user.").build();
			}
		} else {
			return Response.status(Response.Status.FORBIDDEN).entity("Anonymous access denied").build();
		}
	}

	@POST
	@Path("/tmitocarFeedback")
	@Produces(MediaType.TEXT_PLAIN)
	public Response tmitocarFeedbackStatements() {
		TMitocarLRSFeedbackProcessing processing = new TMitocarLRSFeedbackProcessing(feedbackLRSAuth, lrsDomain);
		processing.process();
		
		File statementsFile = new File("AllStatements.csv");

		ResponseBuilder response = Response.ok((Object) statementsFile);  
        response.header("Content-Disposition","attachment; filename=\"AllStatements.csv\"");  
        return response.build();
	}

	/**
	 *
	 * @param multivaluedMap
	 * @return
	 */
	private String getFileName(MultivaluedMap<String, String> multivaluedMap) {

		String[] contentDisposition = multivaluedMap.getFirst("Content-Disposition").split(";");

		for (String filename : contentDisposition) {

			if ((filename.trim().startsWith("filename"))) {
				String[] name = filename.split("=");
				String exactFileName = name[1].trim().replaceAll("\"", "");
				return exactFileName;
			}
		}
		return "UnknownFile";
	}

	/**
	 *
	 * @param inputStream
	 * @param fileName
	 * @throws IOException
	 */
	private String writeToFileServer(InputStream inputStream, String fileName) throws IOException {

		OutputStream outputStream = null;
		String qualifiedUploadFilePath = UPLOAD_FOLDER + fileName;

		try {
			outputStream = new FileOutputStream(new File(qualifiedUploadFilePath));
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			outputStream.flush();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			// release resource, if any
			outputStream.close();
		}
		return qualifiedUploadFilePath;
	}

	// public String checkUserInCourse(String courseid, String userid) {

	// 	if(this.service.courses.get(courseid).getUsers().containsKey(userid){}




	// }

	/**
	 * A function that gets all the courses a tutor can see from a MySQL database.
	 *
	 * @param sub an identification string of the tutor
	 * 
	 * @return an JSONArray converted to a String, that contains the course name and URL
	 * 
	 */
	private String getCourseListFromMysql(String sub) {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection con = DriverManager.getConnection("jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + mysqlDatabase + "?useSSL=false",
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
		} catch (Exception e) {
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
	private String convertHexToString(String hex) {

		StringBuilder sb = new StringBuilder();
		StringBuilder temp = new StringBuilder();

		for (int i = 0; i < hex.length() - 1; i += 2) {
			String output = hex.substring(i, (i + 2));
			int decimal = Integer.parseInt(output, 16);
			// convert the decimal to character
			sb.append((char) decimal);

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
		nameObj.put("$first", "$statement.actor.name");

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
		//DEBUG: System.out.println("Requesting Students of course " + course + " from LRS with JSONArray:\n" + arr);

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
		// match block
		JSONObject matchObj = new JSONObject();
		matchObj.put("statement.object.definition.type", course);
		JSONObject match = new JSONObject();
		match.put("$match", matchObj);

		// project block
		JSONObject projectObj = new JSONObject();
		projectObj.put("_id", "$statement.actor.mbox");
		projectObj.put("statement.object", 1);
		projectObj.put("statement.result.score.scaled", 1);
		projectObj.put("statement.timestamp", 1);
		projectObj.put("statement.result.response", 1);
		JSONObject project = new JSONObject();
		project.put("$project", projectObj);

		// sort block
		JSONObject sortObj = new JSONObject();
		sortObj.put("statement.timestamp", 1);
		JSONObject sort = new JSONObject();
		sort.put("$sort", sortObj);

		// group block
		JSONObject groupObj = new JSONObject();
		groupObj.put("_id", "$_id");
		// averageScore block
		JSONObject averageScoreObj = new JSONObject();
		averageScoreObj.put("$avg", "$statement.result.score.scaled");
		groupObj.put("averageScore", averageScoreObj);
		// results block
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
		//DEBUG: System.out.println("Requesting results of students of course " + course + " from LRS");

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
		System.out.println("Requesting sensor data from LRS");

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
				URL url = new URL(lrsStatementURL + "pipeline=" + pipeline);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
				conn.setRequestProperty("Authorization","Basic " + auth);

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
		try {
			Connection con = connectToDatabase();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("select MOODLE_TOKEN from moodle_lrs_mapping where EMAIL = '" + userEmail + "'");
			while(rs.next()) {
				moodleToken = rs.getString("moodle_token");
			}
			con.close();

			try {
				url = new URL(lrsClientURL);
				HttpURLConnection conn = null;
				conn = (HttpURLConnection) url.openConnection();
				conn.setDoOutput(true);
				conn.setDoInput(true);
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
				conn.setRequestProperty("X-Experience-API-Version", "1.0.3");
				conn.setRequestProperty("Authorization", lrsAuth);
				conn.setRequestProperty("Cache-Control", "no-cache");
				conn.setUseCaches(false);

				InputStream is = conn.getInputStream();
				BufferedReader rd = new BufferedReader(new InputStreamReader(is));
				String line;
				StringBuilder response = new StringBuilder();
				while ((line = rd.readLine()) != null) {
					response.append(line);
				}
				Object obj = JSONValue.parse(response.toString());

				for (int i = 0; i < ((JSONArray) obj).size(); i++) {
					JSONObject client = (JSONObject) ((JSONArray) obj).get(i);
					if (client.get("title").equals(moodleToken)) {
						return client.get("api");
					}
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			System.out.println(e);
			return "Error";
		}

		return "noClientExists";
	}

	private Connection connectToDatabase() {
		Connection con = null;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + mysqlDatabase + "?useSSL=false",
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
			Connection con = DriverManager.getConnection("jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + mysqlDatabase + "?useSSL=false",
					mysqlUser, mysqlPassword);
			
			Statement stmt = con.createStatement();
			//this is where the course ids of the new course should be initialized
			ResultSet rs = stmt.executeQuery("select COURSELINK from ACCESS");
			while(rs.next()) {
				String courseid = rs.getString("COURSELINK");
				System.out.println("(!) Creating course : " + courseid);
				Course course = new MoodleCourse(courseid, courseid, this, this.suggestEvaluator);
				courses.put(courseid, course);
			}
			System.out.println("Course list: " + courses.toString());
			con.close();
		} catch(Exception e) {
			System.out.println(e);
		}
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
	     * A function that is called to check wether a users is initialized in the course list in the Mentoring
		 * Cockpit Service
	     *
	     * @param body Request body of the chatbot
		 * 
		 * Returns either positive or negative formated message string
	     *
	     */
		@POST
	    @Path("/checkStudentStatus")
		@Consumes(MediaType.TEXT_PLAIN)
	    @Produces(MediaType.APPLICATION_JSON)
	    @ApiOperation(
				value = "Check user status in course",
				notes = "Checks wether the user is initialized in the course, and returns a negative or positive message")
	    @ApiResponses(
	            value = { @ApiResponse(
	                    code = HttpURLConnection.HTTP_OK,
	                    message = "Connection works") })
		public Response checkStudentStatus(String body) {
			JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
	    	JSONObject returnObj = new JSONObject();
	    	try {

				//DEBUG: System.out.println("-DEBUG: "+body);
	    		JSONObject bodyObj = (JSONObject) parser.parse(body);
	    		String userid = bodyObj.getAsString("email");
				//DEBUG: System.out.println("-DEBUG: "+userid);
	    		String courseid = bodyObj.getAsString("courseid");
				//DEBUG System.out.println("-DEBUG:"+courseid);

				if (courseid != null) {
					System.out.println("DEBUG: courseid is not null " + courseid);
	    			if (service.courses.containsKey(courseid)) {
						
						if(this.service.courses.get(courseid).getUsers().containsKey(userid)){
							System.out.println("DEBUG: Service has the course initialized");
							returnObj.put("text", "You are initialized in the course and are ready to go :vertical_traffic_light:!");
						}
						else{
							//wait 5 seconds for the proxy to have time
							TimeUnit.SECONDS.sleep(5);
							this.service.courses.get(courseid).update(); 
							System.out.println("-DEBUG: After update status check");
							if(this.service.courses.get(courseid).getUsers().containsKey(userid))
							{
								returnObj.put("text", "You are now initialized in the course! :bulb:");
							}
							else{
								returnObj.put("text", "You are not initialized in the course, interact with an item in the course. If you have already opened and item, wait a little, you should be initialized in a moment! :bulb:");
							}

						}

		    		} 
					else{
						returnObj.put("text", "The course you are attempting to use this bot with is not initialized on the system, please contact an admin");
					}

	    		} else {
					System.out.println("DEBUG: courseid is null");
					String coursesList = "Courses: \n"; 
	    			for (Entry<String, Course> entry : this.service.courses.entrySet()) {

	    				if (entry.getValue().getUsers().containsKey(userid)) {
							coursesList = coursesList +"\n"+entry.getValue().getCourseid();
	    				}
	    			}
					returnObj.put("text", "You are initialized for the following courses  "+ coursesList);
	    		}


				returnObj.put("closeContext", "true");
	    		return Response.status(200).entity(returnObj).build();
			
			}catch (Exception e) {
				e.printStackTrace();
				returnObj.put("text", "Something went wrong checking your status in the course");
				return Response.status(400).entity(returnObj).build();
			}
		}
		/**
	     * A function which takes an audio message codified in base64, user email, and courseid, and 
		 * makes a request to the Emotion Recognition Service to make a prediction and recommendation based on the predicted emotion
	     *
	     * @param body Request body of the chatbot
		 * 
		 * Returns a formated message of the item predictions for the given course and user
	     *
	     */
		@POST
	    @Path("/getEmotionSuggestion")
		@Consumes(MediaType.TEXT_PLAIN)
	    @Produces(MediaType.APPLICATION_JSON)
	    @ApiOperation(
				value = "Get Suggestion",
				notes = "Returns a resource suggestion for the given course and user based on the current and past emotional responses.")
	    @ApiResponses(
	            value = { @ApiResponse(
	                    code = HttpURLConnection.HTTP_OK,
	                    message = "Connection works") })
	    public Response getEmotionSuggestion(String body) {


	    	JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
	    	JSONObject returnObj = new JSONObject();
	    	try {
	    		JSONObject bodyObj = (JSONObject) parser.parse(body);
	    		String userid = bodyObj.getAsString("email");
	    		String courseid = bodyObj.getAsString("courseid");
	    		int numOfSuggestions = bodyObj.getAsNumber("numOfSuggestions").intValue();




				String line = null;
				StringBuilder sb = new StringBuilder ();
				String res = null;
				URL url = UriBuilder.fromPath(this.service.eRecUrl+"/static/emotion/speech/")
							.build()
							.toURL();
				//DEBUG: System.out.println("Attempting connection with url:" + url.toString());
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type", "application-json; utf-8");
				connection.setRequestProperty("Accept", "application/json");
				connection.setDoOutput(true);
				try(OutputStream os = connection.getOutputStream()){

					byte[] input = bodyObj.toJSONString().getBytes("utf-8");
					os.write(input, 0, input.length);		

				}
				connection.connect();
			
				BufferedReader rd  = new BufferedReader( new InputStreamReader(connection.getInputStream(), "UTF-8"));
	
				while ((line = rd.readLine()) != null ) {
					sb.append(line);
				}



				res = sb.toString();

				/*Res string from the Emotion Recognition Service contains: 
				(text): speech to text from the audio file,
				(predicted_emotion): from the emotion recognition service,
				(metadata): userid, time, */

				try{
					System.out.println("Attempting to extract JSON response from Emotion Recognition Service");
					JSONObject bodyObj2 = (JSONObject) parser.parse(res);
					String intent = bodyObj2.getAsString("intent");
					System.out.println("Intent correctly extracted:" + intent);
					/*maxEmotion is the emotion with the highest activation coefficient
					angry, sad, happy, ps(pleasently surprised), neutral*/
					String emotion = bodyObj2.getAsString("emotion");
					String max = bodyObj2.getAsString("max_emotion");
					System.out.println("Emotion: The maximal value from the emotion prediction was: "+ max);
					Emotion maxEmotion = switch (max) {
						case "happy" -> {
							System.out.println("happy!");
							yield Emotion.HAPPY;
						}
						case "ps" -> {
							System.out.println("ps!");
							yield Emotion.PS;
						}
						case "sad" -> {
							System.out.println("sad!");
							yield Emotion.SAD;
						}
						case "neutral" -> {
							System.out.println("neutral!");
							yield Emotion.NEUTRAL;
						}
						default -> {
							System.out.println("default case:!");
							yield Emotion.UNDEFINED;
						}
					};

					JSONObject emotionArray = (JSONObject) parser.parse(emotion);
					double valence = bodyObj2.getAsNumber("valence").doubleValue();
					System.out.println("Valence correctly extracted: "+ valence);

					if (intent.equals("past")){

						System.out.println("-DEBUG: Generating PAST suggestion");
						//updating the valence, not for the functioning of past recommendation, but to keep track in case of a problem with a next iteration future suggestion
						if (courseid != null) {
							if (service.courses.containsKey(courseid)) {
								System.out.println("-DEBUG: Mentoring: courseid identified, trying to get suggestions for user with userid: "+ userid);
								if(this.service.courses.get(courseid).getUsers().containsKey(userid))
								{
									this.service.courses.get(courseid).updateEmotion(userid,valence);
								}
								else{
									returnObj.put("text", "User is not initialized, try interacting with the some item in the course! :pencil:");
								}
							} 
						} else {
							for (Entry<String, Course> entry : this.service.courses.entrySet()) {
								entry.getValue().update();
								if (entry.getValue().getUsers().containsKey(userid)) {
									entry.getValue().updateEmotion(userid,valence);
									break;
								}
							}
						}
						//trigger past suggestions, call function from erec with userid, numOfSuggestions, valence
						url = UriBuilder.fromPath(this.service.limeUrl + "/getLowest")
					 			.build()
								.toURL();
						System.out.println("-DEBUG: Attempting connection with Emotion Recognition Service with :" + url.toString());
						connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod("POST");
						connection.setRequestProperty("Content-Type", "application-json; utf-8");
						connection.setRequestProperty("Accept", "application/json");
						connection.setDoOutput(true);
						try(OutputStream os = connection.getOutputStream()){

							byte[] input = bodyObj2.toJSONString().getBytes("utf-8");
							os.write(input, 0, input.length);

						}
						connection.connect();
					
						sb = new StringBuilder ();
						rd  = new BufferedReader( new InputStreamReader(connection.getInputStream(), "UTF-8"));
			
						while ((line = rd.readLine()) != null ) {
							sb.append(line);
						}
						res = sb.toString();

						JSONObject bodyObj3 = (JSONObject) parser.parse(res);
						Iterator<String> keys = bodyObj3.keySet().iterator(); 
						String resFormated = ""; 
						String key = keys.next(); 
						if (bodyObj3.getAsString(key).equals("No results")){
							returnObj.put("text", "There are not results for your Items, try asking me for the _lime links_ and fill one out :smile: ");
						}
						else{

							do{
								// resFormated = "\r\n" +key +" : "+  bodyObj3.getAsString(key)+"\r\n"+ resFormated;
								resFormated = "\r\n" + TextFormatter.createChatHyperlink(key, bodyObj3.getAsString(key))+ "\r\n "+resFormated;
								if (keys.hasNext()){
									key = keys.next(); 
								}
							}while(keys.hasNext());
	
							returnObj.put("text", " \r\n "+TextFormatter.emotionPast(maxEmotion, valence, resFormated));
						}




					}


					else if (intent.equals("future")) {

						//DEBUG: System.out.println("Future suggestion!");
						if (courseid != null) {
							if (service.courses.containsKey(courseid)) {
								System.out.println("-DEBUG: courseid in list, generating suggestions for user "+ userid);
								if(this.service.courses.get(courseid).getUsers().containsKey(userid))
								{
									this.service.courses.get(courseid).updateEmotion(userid,valence);
									this.service.courses.get(courseid).update();
									returnObj.put("text", this.service.courses.get(courseid).getSuggestionFuture(userid, valence, maxEmotion, numOfSuggestions));
								}
								else{
									returnObj.put("text", "User is not initialized, try interacting with some item in the course! :pencil:");
								}
							} 
						} else {
							for (Entry<String, Course> entry : this.service.courses.entrySet()) {
								entry.getValue().update();
								if (entry.getValue().getUsers().containsKey(userid)) {
									returnObj.put("text","--debug:\r\n"+ emotionArray+"\r\n"+  entry.getValue().getSuggestionFuture(userid, valence, maxEmotion,numOfSuggestions));
									break;
								}
							}
						}
	
					}
					else{
						System.out.println("Not intent found in the request");
						returnObj.put("text", "I did not understand what you wanted, please try again :smile: ");
					}



				}catch (Exception e){
					e.printStackTrace();
					returnObj.put("text", "Something went wrong retrieving the data from the Emotion Recognition Service");
				}

				

	    		if (!returnObj.containsKey("text")) {
    				returnObj.put("text", res +"(!) Error: Something went wrong");
    			}
	    		returnObj.put("closeContext", "true");
	    		return Response.status(200).entity(returnObj).build();
	    		
	    		
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    		returnObj.put("text", "The connection with the emotion recognition service was not possible");
	    		return Response.status(400).entity(returnObj).build();
	    	}
	    	
		}

		/**
		 * A function which takes an audio message codified in base64, user email, and courseid, and
		 * makes a request to the Emotion Recognition Service to make a prediction and recommendation based on the predicted emotion
		 *
		 * @param body Request body of the chatbot
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
				String userid;
				boolean html;
				if (bodyObj.get("moodlebot") == null) {
					userid = bodyObj.getAsString("email");
					html = false;
				} else {
					userid = bodyObj.getAsString("user");

					//temporary solution to get the mail of the user which is used as ID in the triplestore (Issue #6)
					try {
						URL url = new URL("https://moodle.tech4comp.dbis.rwth-aachen.de/webservice/rest/server.php?wstoken="
								+ this.service.moodleToken
								+ "&moodlewsrestformat=json&wsfunction=core_user_get_users_by_field&field=id&values[0]="
								+ userid);
						HttpURLConnection conn = (HttpURLConnection) url.openConnection();
						conn.setRequestMethod("POST");
						conn.setUseCaches(false);

						InputStream is = conn.getInputStream();
						BufferedReader rd = new BufferedReader(new InputStreamReader(is));
						String line;
						StringBuilder response = new StringBuilder();
						while ((line = rd.readLine()) != null) {
							response.append(line);
						}
						rd.close();
						JSONObject obj = (JSONObject) ((JSONArray) JSONValue.parse(response.toString())).get(0);

						userid = obj.getAsString("email");
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

					html = true;
				}
				String courseid = bodyObj.getAsString("courseid");
				int numOfSuggestions = bodyObj.getAsNumber("numOfSuggestions").intValue();
				if (courseid != null) {
					System.out.println("DEBUG: CourseID is not null" + courseid);
	    			if (service.courses.containsKey(courseid)) {
						System.out.println("DEBUG: Service has the course initialized");
						this.service.courses.get(courseid).update();
		    			returnObj.put("text", this.service.courses.get(courseid).getSuggestion(userid, numOfSuggestions, html));
		    		} 
	    		} else {
					System.out.println("DEBUG: Course ID is null");
	    			for (Entry<String, Course> entry : this.service.courses.entrySet()) {
	    				entry.getValue().update();
	    				if (entry.getValue().getUsers().containsKey(userid)) {
	    					returnObj.put("text", entry.getValue().getSuggestion(userid, numOfSuggestions, html));
	    					break;
	    				}
	    			}
	    		}
	    		if (!returnObj.containsKey("text")) {
    				returnObj.put("text", "Error: Course not initialized!");
    			}
	    		returnObj.put("closeContext", "true");
	    		return Response.status(200).entity(returnObj).build();
	    		
	    		
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    		returnObj.put("text", "I wasn't able to understand your message very well. Would you mind reformulating it?");
	    		return Response.status(400).entity(returnObj).build();
	    	}
	    	
		}
	    
	    /**
	     * A function that is called by a chatbot to generate a list of recommendations based on the given theme.
	     *
	     * @param body Request body of the chatbot
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
	    		System.out.println("Incoming message body for suggestionByTheme:\n" + body);
	    		JSONObject bodyObj = (JSONObject) parser.parse(body);
				boolean html;
				if (bodyObj.get("moodlebot") == null) {
					html = false;
				} else {
					html = true;
				}
	    		
//	    		JSONObject themeEntity = null;
//	    		System.out.println("Trying to convert to JSONArray:\n" + bodyObj);
//	    		JSONArray entities = (JSONArray) bodyObj.get("entities");
				JSONObject entity = (JSONObject) bodyObj.get("entities");
	    		if (entity != null && !entity.keySet().isEmpty()) {
//	    			for (int i = 0; i < entities.size(); i++) {
//		    			JSONObject entity = (JSONObject) entities.get(i);
//		    			if (themeEntity == null || entity.getAsNumber("confidence").doubleValue() > themeEntity.getAsNumber("confidence").doubleValue()) {
//		    				themeEntity = entity;
//		    			}
//		    		}
		    		String courseid = bodyObj.getAsString("courseid");
		    		if (service.courses.containsKey(courseid)) {
		    			// There should be only one entity in there
						String entityName = (String) entity.keySet().iterator().next();
						//todo (from ba_stuecker): This is the part where i could get the links to the different topics, it needed in the end
						returnObj.put("text", this.service.courses.get(courseid).getThemeSuggestions(entityName, html));
		    		} else {
		    			returnObj.put("text", "Error: Course " + courseid + " not initialized!");
		    		}
	    		} else {
	    			returnObj.put("text", "I wasn't able to understand your message very well. Would you mind reformulating it?");
	    		}
	    		
	    		returnObj.put("closeContext", "true");
	    		//DEBUG: System.out.println("suggestionByTheme returns:\n" + returnObj);
	    		return Response.status(200).entity(returnObj).build();
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    		returnObj.put("text", "Error");
	    		return Response.status(400).entity(returnObj).build();
	    	}
	    	
		}
		/**
	     * A function which generates a personalized link to fill a Item lime questionnaire for Affibot
		 * it routes the request to the lime-proxy service
	     *
	     * @param body Request body of the chatbot
		 * 
		 * Returns a formated list of the lime links for the user identified in the request json, or an error text
	     *
	     */
		@POST
		@Path("/getLimeLink")
		@Consumes(MediaType.TEXT_PLAIN)
	    @Produces(MediaType.APPLICATION_JSON)
	    @ApiOperation(
				value = "Request the Lime questionnaire link for a user",
				notes = "Return 200 for a valid connection")
	    @ApiResponses(
	            value = { @ApiResponse(
	                    code = HttpURLConnection.HTTP_OK,
	                    message = "Connection works") })
		public Response getLimeLink(String body) {
	    	JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
	    	JSONObject returnObj = new JSONObject();


			try {
	    		JSONObject bodyObj = (JSONObject) parser.parse(body);
	    		String userid = bodyObj.getAsString("email");
				String line = null;
				StringBuilder sb = new StringBuilder ();
				String res = null;
				URL url = UriBuilder.fromPath(this.service.limeUrl + "/get_link")
							.build()
							.toURL();
				//DEBUG: System.out.println("Attempting connection with url:" + url.toString());
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				connection.setRequestProperty("Content-Type", "application-json; utf-8");
				connection.setRequestProperty("Accept", "application/json");
				connection.setDoOutput(true);
				try(OutputStream os = connection.getOutputStream()){

					byte[] input = bodyObj.toJSONString().getBytes("utf-8");
					os.write(input, 0, input.length);		

				}
				connection.connect();
			
				BufferedReader rd  = new BufferedReader( new InputStreamReader(connection.getInputStream(), "UTF-8"));
	
				while ((line = rd.readLine()) != null ) {
					sb.append(line);
				}




				res = sb.toString();

				
				JSONObject bodyObj2 = (JSONObject) parser.parse(res);
				Iterator<String> keys = bodyObj2.keySet().iterator(); 
				String resFormated = ""; 
				String key = keys.next();
				if(bodyObj2.getAsString(key).equals("Not initialized")){
					returnObj.put("text", "The user is not initialized for the lime links! Contact the administrator with : juan.stuecker@rwth-aachen.de");
				}
				else{
					do{
						resFormated = resFormated +"\r\n"+TextFormatter.createChatHyperlink(key, bodyObj2.getAsString(key));
						key = keys.next(); 
					}while(keys.hasNext());
					returnObj.put("text", resFormated);
				}

			

				return Response.status(400).entity(returnObj).build();



		}catch (Exception e) {
			e.printStackTrace();
			returnObj.put("text", "Error");
			return Response.status(400).entity(returnObj).build();
		}
		
		
	}


	}
	
	
}
