package uk.co.bluesuntech.export;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

import uk.co.bluesuntech.autoscaling.AutoscaleClient;
import uk.co.bluesuntech.ec2.EC2Client;
import uk.co.bluesuntech.rds.RDSClient;


public class Exporter {
	public JSONObject exportExploration() throws JSONException {
		EC2Client ec2Client = new EC2Client();
		RDSClient rdsClient = new RDSClient();
		AutoscaleClient autoscaleClient = new AutoscaleClient();
		
		JSONObject configuration = new JSONObject();
		
		// EC2
		JSONObject ec2Config = new JSONObject();
		List<JSONObject> ec2Instances = ec2Client.getInstancesAsJson();
		List<JSONObject> ec2SecurityGroups = ec2Client.getSecurityGroupsAsJson();
		ec2Config.put("instances", ec2Instances);
		ec2Config.put("securityGroups", ec2SecurityGroups);
		configuration.put("ec2", ec2Config);
		
		// RDS
		JSONObject rdsConfig = new JSONObject();
		List<JSONObject> rdsInstances = rdsClient.getRDSInstancesAsJson();
		rdsConfig.put("instances", rdsInstances);
		configuration.put("rds", rdsConfig);
		
		// Autoscale
		JSONObject autoscaleConfig = new JSONObject();
		List<JSONObject> autoscaleAlarms = autoscaleClient.getAlarmsAsJson();
		List<JSONObject> autoscaleGroups = autoscaleClient.getGroupsAsJson();
		List<JSONObject> autoscaleLaunchConfigs = autoscaleClient.getLaunchConfigurationsAsJson();
		List<JSONObject> autoscalePolicies = autoscaleClient.getAutoscalingPoliciesAsJson();
		
		autoscaleConfig.put("alarms", autoscaleAlarms);
		autoscaleConfig.put("groups", autoscaleGroups);
		autoscaleConfig.put("launchConfigs", autoscaleLaunchConfigs);
		autoscaleConfig.put("policies", autoscalePolicies);
		configuration.put("autoscale", autoscaleConfig);
		
		return configuration;
	}
	
	public JSONObject createEmptyConfig() throws JSONException{
		JSONObject configuration = new JSONObject();
		JSONObject ec2Config = new JSONObject();
		JSONObject rdsConfig = new JSONObject();
		
		ec2Config.put("instances", new JSONObject());
		ec2Config.put("securityGroups", new JSONObject());
		
		rdsConfig.put("instances", new JSONObject());
		
		configuration.put("ec2", ec2Config);
		configuration.put("rds", rdsConfig);
		
		return configuration;
	}
	
	public void writeConfig(String fileName, JSONObject configuration) throws IOException, JSONException {
		Path path = Paths.get(fileName);
		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
		    writer.write(configuration.toString(4));
		}
	}
}
