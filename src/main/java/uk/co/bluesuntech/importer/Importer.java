package uk.co.bluesuntech.importer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import uk.co.bluesuntech.delta.EC2SecurityGroupDelta;

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
	
	public JSONObject createDelta(JSONObject currentConfig, JSONObject existingConfig) throws JSONException {
		/* currentConfig is the config loaded from file. existingConfig is the config that actually
		 * exists on AWS.
		 */
		
		JSONObject fullDelta = new JSONObject();
		
		// EC2
		JSONObject ec2Delta = new JSONObject();
		JSONObject sgDelta = new EC2SecurityGroupDelta().getSecurityGroupDelta(currentConfig, existingConfig);
		ec2Delta.put("securityGroups", sgDelta);

		// Get new instances
		// Get deleted instances
		
		// Put add everything to delta
		fullDelta.put("ec2", ec2Delta);
		
		return fullDelta;
	}
}
