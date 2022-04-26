package i5.las2peer.services.mentoringCockpitService;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.connectors.webConnector.WebConnector;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.p2p.LocalNode;
import i5.las2peer.p2p.LocalNodeManager;
import i5.las2peer.security.UserAgentImpl;
import i5.las2peer.testing.MockAgentFactory;
import net.minidev.json.JSONObject;

/**
 * Example Test Class demonstrating a basic JUnit test structure.
 *
 */
public class ServiceTest {

	private static LocalNode node;
	private static WebConnector connector;
	private static ByteArrayOutputStream logStream;

	private static UserAgentImpl testAgent;
	private static final String testPass = "adamspass";
	private static final String mainPath = "mentoring/";

	/**
	 * Called before a test starts.
	 * 
	 * Sets up the node, initializes connector and adds user agent that can be used throughout the test.
	 * 
	 * @throws Exception
	 */
	@Before
	public void startServer() throws Exception {
		// start node
		node = new LocalNodeManager().newNode();
		node.launch();

		// add agent to node
		testAgent = MockAgentFactory.getAdam();
		testAgent.unlock(testPass); // agents must be unlocked in order to be stored
		node.storeAgent(testAgent);

		// start service
		// during testing, the specified service version does not matter
		node.startService(new ServiceNameVersion(MentoringCockpitService.class.getName(), "1.0"), "a pass");

		// start connector
		connector = new WebConnector(true, 0, false, 0); // port 0 means use system defined port
		logStream = new ByteArrayOutputStream();
		connector.setLogStream(new PrintStream(logStream));
		connector.start(node);
	}

	/**
	 * Called after the test has finished. Shuts down the server and prints out the connector log file for reference.
	 * 
	 * @throws Exception
	 */
	@After
	public void shutDownServer() throws Exception {
		if (connector != null) {
			connector.stop();
			connector = null;
		}
		if (node != null) {
			node.shutDown();
			node = null;
		}
		if (logStream != null) {
			System.out.println("Connector-Log:");
			System.out.println("--------------");
			System.out.println(logStream.toString());
			logStream = null;
		}
	}

	/**
	 * 
	 * Tests the validation method.
	 * 
	 */
	@Test
	public void testCourseCreation() {
//		try {
//			while (true) {Thread.sleep(1000);}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		// Connect to service
		MiniClient c = new MiniClient();
		c.setLogin(testAgent.getIdentifier(), testPass);
		c.setConnectorEndpoint(connector.getHttpEndpoint());
		JSONObject body = new JSONObject();
		
//		JSONArray entities = new JSONArray();
//		JSONObject entity1 = new JSONObject();
//		JSONObject entity2 = new JSONObject();
//		
//		entity1.put("entityName", "barabasiAlbert");
//		entity1.put("value", "barabasiAlbert");
//		entity1.put("confidence", 0.5);
//		
//		entity2.put("entityName", "mapreduce");
//		entity2.put("value", "mapreduce");
//		entity2.put("confidence", 0.4);
//		
//		entities.add(entity1);
//		entities.add(entity2);
//		
//		
//		body.put("entities", entities);
//		body.put("courseid", "18");
//		ClientResponse response = c.sendRequest("GET", mainPath + "assignBots", body.toJSONString());
//		System.out.println(response.getResponse());
		
//		body.put("firstEntity", "cloudComputingIntro");
//		response = c.sendRequest("POST", mainPath + "suggestions/getSuggestionByTheme", body.toJSONString());
//		System.out.println(response.getResponse());
		
		for (int i = 0; i < 6; i++) {
			
			body.put("user", "https://moodle.tech4comp.dbis.rwth-aachen.de/user/profile.php?id=180");
			//body.put("courseid", "https://moodle.tech4comp.dbis.rwth-aachen.de/course/view.php?id=20");
			body.put("numOfSuggestions", "2");
			ClientResponse response = c.sendRequest("POST", mainPath + "suggestions/getSuggestion", body.toJSONString());
			System.out.println(response.getResponse());
		}
		
		
		
//		System.out.println("DEBUG --- Testing starts");
		//MentoringCockpitService service = new MentoringCockpitService();
		//service.createCourses();
		//service.courses.get("18").createThemes(0);
//		if (!service.courses.isEmpty()) {
//			System.out.println("DEBUG --- Course" + service.courses.keySet().toString());
//			System.out.println("DEBUG --- Success");
//		}
//	
//		service.courses.get("18").createUsers();
//		System.out.println("DEBUG --- Users: " + service.courses.get("18").getUsers().keySet().toString());
//		service.courses.get("18").createResources();
//		System.out.println("DEBUG --- Resources: " + service.courses.get("18").getResources().keySet().toString());
//		service.courses.get("18").createRelations();
//		System.out.println("DEBUG --- Completed: " + service.courses.get("18").getUsers().get("damatta.developer@gmail.com").getCompletedResources().keySet().toString());
//		System.out.println("DEBUG --- Grade: " + service.courses.get("18").getUsers().get("damatta.developer@gmail.com").getGrades().get("https://moodle.tech4comp.dbis.rwth-aachen.demod/quiz/view.php?id=200").toString());
	}
}
