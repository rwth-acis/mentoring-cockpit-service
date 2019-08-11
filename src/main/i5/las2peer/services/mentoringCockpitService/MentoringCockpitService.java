package i5.las2peer.services.mentoringCockpitService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import i5.las2peer.api.ManualDeployment;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

// TODO Adjust the following configuration
@Api
@SwaggerDefinition(
		info = @Info(
				title = "las2peer Template Service",
				version = "1.0.0",
				description = "A las2peer Template Service for demonstration purposes.",
				termsOfService = "http://your-terms-of-service-url.com",
				contact = @Contact(
						name = "John Doe",
						url = "provider.com",
						email = "john.doe@provider.com"),
				license = @License(
						name = "your software license name",
						url = "http://your-software-license-url.com")))

@ManualDeployment
@ServicePath("/mentoring")
public class MentoringCockpitService extends RESTService {

	private String mcToken;
	private String courseURL;
	private String lrsDomain;
	private String lrsAuth;
	
	public MentoringCockpitService() {
		setFieldValues();
		//TODO allow multiple users/courses
		System.out.println();
	}
	
	@GET
	@Path("/{token}/students")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Connection works") })
	public Response getStudents(@PathParam("token") String token) {
		if(token.equals(mcToken)) {
			String response = getStudentsByCourse(courseURL);
			return Response.ok().entity(response).build();
		} else {
			return Response.status(401).entity("Unauthorized").build();
		}
	}

	@GET
	@Path("/{token}/results")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Connection works") })
	public Response getResults(@PathParam("token") String token) {
		if(token.equals(mcToken)) {
			String response = getResultsByCourse(courseURL);
			return Response.ok().entity(response).build();
		} else {
			return Response.status(401).entity("Unauthorized").build();
		}
	}

	private String getStudentsByCourse(String course) {
		JSONObject typeObj = new JSONObject();
		typeObj.put("statement.object.definition.type", course);
		
		JSONObject matchObj = new JSONObject();
		matchObj.put("$match", typeObj);
		
		JSONObject nameObj = new JSONObject();
		nameObj.put("$first","$statement.actor.name");

		JSONObject courseNameObj = new JSONObject();
		courseNameObj.put("$first","$statement.object.definition.description.en-US");
		
		JSONObject actorObj = new JSONObject();
		actorObj.put("name", nameObj);
		actorObj.put("_id", "$statement.actor.mbox");
		actorObj.put("courseName", courseNameObj);
		
		JSONObject groupObj = new JSONObject();
		groupObj.put("$group", actorObj);
		
		JSONArray arr = new JSONArray();
		arr.add(matchObj);
		arr.add(groupObj);
		
		StringBuilder sb = new StringBuilder();
		for (byte b : arr.toString().getBytes()) {
			sb.append("%" + String.format("%02X", b));
		}
		
		StringBuffer response = new StringBuffer();
		
		try {
			URL url = new URL(lrsDomain + "pipeline=" + sb.toString());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			conn.setRequestProperty("Authorization", lrsAuth);
			
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
		
		StringBuffer response = new StringBuffer();
		
		try {
			URL url = new URL(lrsDomain + "pipeline=" + sb.toString());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			conn.setRequestProperty("Authorization", lrsAuth);
			
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

}
