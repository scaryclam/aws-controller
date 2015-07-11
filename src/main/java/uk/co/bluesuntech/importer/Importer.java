package uk.co.bluesuntech.importer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
}
