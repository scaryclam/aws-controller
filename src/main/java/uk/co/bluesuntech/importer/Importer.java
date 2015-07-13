package uk.co.bluesuntech.importer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class Importer {
	public JSONObject readConfigFromFile(String fileName) throws IOException, JSONException {
		Path path = Paths.get(fileName);
		
		String content = new String(Files.readAllBytes(path));
		JSONObject importedJson = new JSONObject(content);
		System.out.println("JSON Imported");
		System.out.println(importedJson.toString(4));
		return importedJson;
	}
	
	public void createDelta(JSONObject newConfig, JSONObject currentConfig) throws JSONException {
		// EC2
		JSONObject ec2Delta = new JSONObject();
		// Get deleted SGs
		List<String> deletedGroups = new ArrayList<String>();
		JSONObject ec2Config = (JSONObject) currentConfig.get("ec2");
		JSONArray securityGroups = (JSONArray) ec2Config.get("securityGroups");
		for (int i = 0; i < securityGroups.length(); i++) {
			JSONObject securityGroup = securityGroups.getJSONObject(i);
			System.out.println(securityGroup.toString());
		}
		// Get new SGs
		
		
		
		
		
		// Get new instances
		// Get deleted instances
	}
}
