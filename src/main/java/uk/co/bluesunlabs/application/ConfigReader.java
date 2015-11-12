package uk.co.bluesunlabs.application;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class ConfigReader {
	private JSONObject template;
	
	public ConfigReader(String configFileLocation) throws IOException, JSONException {
		Path path = Paths.get(configFileLocation);
		String content = new String(Files.readAllBytes(path));
		JSONObject completeConfig = new JSONObject(content);
		JSONObject topConfig = completeConfig.getJSONObject("top");
		String templateName = topConfig.getString("defaultTemplate");
		JSONObject templateConfigs = completeConfig.getJSONObject("templateConfigs");
		JSONObject namedTemplateConfig = templateConfigs.getJSONObject(templateName);
		
		System.out.println(" ====== Read Config: \n" + namedTemplateConfig.toString(4));
		JSONObject template = completeConfig.getJSONObject("template");
		
		JSONObject parsedConfig = createTemplate(template, namedTemplateConfig);
	}
	
	public ConfigReader(String configFileLocation, String templateName) throws IOException, JSONException {
		Path path = Paths.get(configFileLocation);
		String content = new String(Files.readAllBytes(path));
		JSONObject completeConfig = new JSONObject(content);
		JSONObject topConfig = completeConfig.getJSONObject("top");
		JSONObject templateConfigs = completeConfig.getJSONObject("templateConfigs");
		JSONObject namedTemplateConfig = templateConfigs.getJSONObject(templateName);
		
		System.out.println(" ====== Read Config: \n" + namedTemplateConfig.toString(4));
		JSONObject template = completeConfig.getJSONObject("template");
		
		JSONObject parsedConfig = createTemplate(template, namedTemplateConfig);
		setTemplate(parsedConfig);
	}
	
	public JSONObject getEC2Config(JSONObject config) throws JSONException {
		JSONObject ec2Config = config.getJSONObject("ec2");
		return ec2Config;
	}
	
	public JSONObject getTemplate() {
		return template;
	}

	public void setTemplate(JSONObject template) {
		this.template = template;
	}

	private JSONObject createTemplate(JSONObject template, JSONObject context) throws JSONException {
		String templateString = template.toString();
		VelocityEngine engine = new VelocityEngine();
		VelocityContext newContext = new VelocityContext();
		
		Iterator<?> contextKeys = context.keys();
		while (contextKeys.hasNext()) {
			String key = (String) contextKeys.next();
			newContext.put(key, context.get(key));
		}
		
		StringWriter writer = new StringWriter();
		Velocity.evaluate(newContext, writer, "TAG", templateString);
		
		JSONObject templatedConfig = new JSONObject(writer.toString());
		System.out.println("Result 123: " + templatedConfig.toString(4));
		return templatedConfig;
	}
}
