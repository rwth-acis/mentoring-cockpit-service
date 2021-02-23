package i5.las2peer.services.mentoringCockpitService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TMitocarLRSFeedbackProcessing {
    private String feedbackLRSAuth;
    private String feedbackLRSDomain;

    
    public TMitocarLRSFeedbackProcessing(String feedbackLRSAuth, String feedbackLRSDomain) {
        this.feedbackLRSAuth = feedbackLRSAuth;
        this.feedbackLRSDomain = feedbackLRSDomain;
    }

    private String LRSConnection(String lrsDomain, String lrsAuth) {
        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL(lrsDomain);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Authorization", lrsAuth);
            conn.setRequestProperty("X-Experience-API-Version", "1.0.3");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response.toString();
    }

    private Map<String, String> extractDataFromStatement(JSONObject statement) throws JSONException {
        Map<String, String> retVal = new HashMap<>();
        JSONObject actor = statement.getJSONObject("actor");
        JSONObject actorAccount = actor.getJSONObject("account");
        JSONObject object = statement.getJSONObject("object");
        JSONObject objectDefinition = object.getJSONObject("definition");
        JSONObject definitionName = objectDefinition.getJSONObject("name");
        JSONObject context = statement.getJSONObject("context");
        JSONObject extensions = context.getJSONObject("extensions");
        JSONObject feedbackCompare = extensions.getJSONObject("https://tech4comp.de/xapi/context/extensions/feedback_compare");
        JSONObject feedbackTasa = extensions.getJSONObject("https://tech4comp.de/xapi/context/extensions/feedback_TASA");
        JSONObject feedbackMStats = extensions.getJSONObject("https://tech4comp.de/xapi/context/extensions/feedback_Mstats");
        JSONObject feedbackLength = extensions.getJSONObject("https://tech4comp.de/xapi/context/extensions/feedback_length");
        JSONObject feedbackTimestamp = extensions.getJSONObject("https://tech4comp.de/xapi/context/extensions/feedback_TUpload");

        retVal.put("email", actorAccount.getString("name"));
        retVal.put("theme", definitionName.getString("en-US"));

        String tmpID = object.getString("id");
        String[] idParts = tmpID.split("/");
        retVal.put("id", idParts[idParts.length - 1]);

        retVal.put("CONC", String.valueOf(feedbackCompare.getFloat("CONC")));
        retVal.put("PROP", String.valueOf(feedbackCompare.getFloat("PROP")));
        retVal.put("BSM", String.valueOf(feedbackCompare.getFloat("BSM")));
        retVal.put("SUR", String.valueOf(feedbackCompare.getFloat("SUR")));
        retVal.put("GRA", String.valueOf(feedbackCompare.getFloat("GRA")));
        retVal.put("STRU", String.valueOf(feedbackCompare.getFloat("STRU")));
        retVal.put("GAMMA", String.valueOf(feedbackCompare.getFloat("GAMMA")));

        retVal.put("NBegriffeDiffA", String.valueOf(feedbackTasa.getInt("NBegriffeDiffA")));
        retVal.put("NBegriffeDiffB", String.valueOf(feedbackTasa.getInt("NBegriffeDiffB")));
        retVal.put("NBegriffeSchnittmenge", String.valueOf(feedbackTasa.getInt("NBegriffeSchnittmenge")));

        retVal.put("surface", String.valueOf(feedbackMStats.getInt("surface")));
        retVal.put("durchmesser", String.valueOf(feedbackMStats.get("durchmesser")));
        retVal.put("stru-downtrace", String.valueOf(feedbackMStats.getInt("stru-downtrace")));
        retVal.put("gamma_adj", String.valueOf(feedbackMStats.getFloat("gamma_adj")));
        retVal.put("gamma_roh", String.valueOf(feedbackMStats.getFloat("gamma_roh")));
        retVal.put("saetze", String.valueOf(feedbackMStats.getInt("saetze")));
        retVal.put("nbegriffe", String.valueOf(feedbackMStats.getInt("nbegriffe")));

        retVal.put("length", String.valueOf(feedbackLength.getInt("length")));
        retVal.put("timestamp", feedbackTimestamp.getString("timestamp"));

        return retVal;
    }

    private void convertToCSV(List<Map<String, String>> data) throws IOException {
        //TODO: change file dump location

        FileWriter csvWriter = new FileWriter("AllStatements.csv");

        String headerRow = "Email,Theme,ID,CONC,PROP,BSM,SUR,GRA,STRU,GAMMA,NBegriffeDiffA,NBegriffeDiffB,NBegriffeSchnittmenge";
        headerRow = headerRow + ",surface,durchmesser,stru-downtrace,gamma_adj,gamma_roh,saetze,nbegriffe,length,timestamp";

        csvWriter.append(headerRow);
        csvWriter.append("\n");

        for (Map<String,String> rowMap : data) {
            csvWriter.append(rowMap.get("email"));
            csvWriter.append(",");
            csvWriter.append(rowMap.get("theme"));
            csvWriter.append(",");
            csvWriter.append(rowMap.get("id"));
            csvWriter.append(",");
            csvWriter.append(rowMap.get("CONC"));
            csvWriter.append(",");
            csvWriter.append(rowMap.get("PROP"));
            csvWriter.append(",");
            csvWriter.append(rowMap.get("BSM"));
            csvWriter.append(",");
            csvWriter.append(rowMap.get("SUR"));
            csvWriter.append(",");
            csvWriter.append(rowMap.get("GRA"));
            csvWriter.append(",");
            csvWriter.append(rowMap.get("STRU"));
            csvWriter.append(",");
            csvWriter.append(rowMap.get("GAMMA"));
            csvWriter.append(",");
            csvWriter.append(rowMap.get("NBegriffeDiffA"));
            csvWriter.append(",");
            csvWriter.append(rowMap.get("NBegriffeDiffB"));
            csvWriter.append(",");
            csvWriter.append(rowMap.get("NBegriffeSchnittmenge"));
            csvWriter.append(",");
            csvWriter.append(rowMap.get("surface"));
            csvWriter.append(",");
            csvWriter.append(rowMap.get("durchmesser"));
            csvWriter.append(",");
            csvWriter.append(rowMap.get("stru-downtrace"));
            csvWriter.append(",");
            csvWriter.append(rowMap.get("gamma_adj"));
            csvWriter.append(",");
            csvWriter.append(rowMap.get("gamma_roh"));
            csvWriter.append(",");
            csvWriter.append(rowMap.get("saetze"));
            csvWriter.append(",");
            csvWriter.append(rowMap.get("nbegriffe"));
            csvWriter.append(",");
            csvWriter.append(rowMap.get("length"));
            csvWriter.append(",");
            csvWriter.append(rowMap.get("timestamp"));
            csvWriter.append("\n");
        }
        csvWriter.append("\n");
        csvWriter.flush();
        csvWriter.close();
    }

    public void process() {
        String res = LRSConnection(feedbackLRSDomain + "/data/xAPI/statements", feedbackLRSAuth);

        try {
            JSONObject root = new JSONObject(res);
            List<Map<String, String>> mapList = new ArrayList<>();


            JSONArray responseStatements = root.getJSONArray("statements");
            for (Object element : responseStatements) {
                JSONObject statement = (JSONObject) element;
                Map<String, String> tmp = extractDataFromStatement(statement);
                mapList.add(tmp);
            }

            int i = 0;
            String pathEnd = root.getString("more");
            while (!pathEnd.equals("")) {
                res = LRSConnection(feedbackLRSDomain + pathEnd, feedbackLRSAuth);
                root = new JSONObject(res);
                responseStatements = root.getJSONArray("statements");
                for (Object element : responseStatements) {
                    JSONObject statement = (JSONObject) element;
                    Map<String, String> tmp = extractDataFromStatement(statement);
                    mapList.add(tmp);
                }
                System.out.println(i++);

                pathEnd = root.getString("more");
            }

            convertToCSV(mapList);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

}
