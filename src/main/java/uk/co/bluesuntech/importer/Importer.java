package uk.co.bluesuntech.importer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import uk.co.bluesuntech.delta.AutoscaleDelta;
import uk.co.bluesuntech.delta.EC2InstanceDelta;
import uk.co.bluesuntech.delta.EC2SecurityGroupDelta;
import uk.co.bluesuntech.delta.RDSInstanceDelta;

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
	
	public JSONObject getEnvironmentConfig(JSONObject configuration, String environment) throws JSONException {
	    if (!configuration.has(environment)) {
	        return createEmptyEnv();
	    }
	    return configuration.getJSONObject(environment);
	}
	
	private JSONObject createEmptyEnv() throws JSONException {
        JSONObject configuration = new JSONObject();
        JSONObject ec2Config = new JSONObject();
        JSONObject rdsConfig = new JSONObject();
        
        ec2Config.put("instances", new JSONArray());
        ec2Config.put("securityGroups", new JSONArray());
        
        rdsConfig.put("instances", new JSONArray());
        
        configuration.put("ec2", ec2Config);
        configuration.put("rds", rdsConfig);
        return configuration;
    }
	
	public JSONObject createDelta(JSONObject currentConfig, JSONObject existingConfig) throws JSONException {
		/* currentConfig is the config loaded from file. existingConfig is the config that actually
		 * exists on AWS.
		 */
		
		JSONObject fullDelta = new JSONObject();
		
		// EC2
		JSONObject ec2Delta = new JSONObject();
		JSONObject sgDelta = new EC2SecurityGroupDelta().getSecurityGroupDelta(currentConfig, existingConfig);
		JSONObject instanceDelta = new EC2InstanceDelta().getInstancesDelta(currentConfig, existingConfig);
		ec2Delta.put("securityGroups", sgDelta);
		ec2Delta.put("instances", instanceDelta);
		
		// RDS
		JSONObject rdsDelta = new JSONObject();
		JSONObject rdsInstanceDelta = new RDSInstanceDelta().getInstancesDelta(currentConfig, existingConfig);
		rdsDelta.put("instances", rdsInstanceDelta);
		
		// Autoscaling
		JSONObject autoscaleDelta = new JSONObject();
		JSONObject autoscaleAlarmDelta = new AutoscaleDelta().getAlarmDelta(currentConfig, existingConfig);
		
		// Put add everything to delta
		fullDelta.put("ec2", ec2Delta);
		fullDelta.put("rds", rdsDelta);
		fullDelta.put("autoscale", autoscaleDelta);
		
		return fullDelta;
	}
}
