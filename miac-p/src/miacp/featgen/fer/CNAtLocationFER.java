package miacp.featgen.fer;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import miacp.util.ConceptNetUtil;

/**
 * Feature extraction rule for producing WordNet synonyms
 */
public class CNAtLocationFER extends AbstractFeatureRule {

	private static final long serialVersionUID = 1L;

	private final String Directory = "data/ConceptNet5";	
	
	public Set<String> getProductions(String input, String type, Set<String> productions) {

		String filePath = Directory + "/" + input + ".json";
		File f = new File(filePath);
		JSONObject jsonData;

		try {
			if (f.exists() && !f.isDirectory()) {
				jsonData = new JSONObject(Files.readAllLines(Paths.get(filePath)).get(0));
			} else {

				// Pulling JSON from web
				String url = "http://api.conceptnet.io/c/en/" + input + "?limit=20";
				// jsonData = readJsonFromUrl(url);
				String jsonString = Jsoup.connect(url).header("Accept", "text/javascript").get().body().text();
				jsonData = new JSONObject(jsonString);
				// Saving the json to a file for future use.
				FileWriter file = new FileWriter(filePath);
				file.write(jsonData.toString());
				file.flush();
				file.close();
			}
			
			JSONArray jsonArray = (JSONArray) jsonData.get("edges");
			for (int i = 0, size = jsonArray.length(); i < size; i++) {
				JSONObject objectInArray = (JSONObject) jsonArray.get(i);
				String id = (String) objectInArray.get("@id");
				productions.addAll(ConceptNetUtil.getRelationWords(id, "AtLocation",input));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
		
		return productions;
	}

}
