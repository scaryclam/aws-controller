package uk.co.bluesunlabs.delta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import uk.co.bluesunlabs.autoscaling.AutoscaleClient;
import uk.co.bluesunlabs.ec2.EC2Client;
import uk.co.bluesunlabs.rds.RDSClient;

import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class DeltaApplier {
	public void applyDelta(JSONObject delta) throws JSONException {
		System.out.println("Applying Changes");
		
		// EC2
		EC2Client ec2Client = new EC2Client();
		JSONObject ec2Delta = delta.getJSONObject("ec2");
		JSONObject securityGroupDelta = ec2Delta.getJSONObject("securityGroups");
		JSONObject ec2InstanceDelta = ec2Delta.getJSONObject("instances");
		JSONArray sgAdditions = securityGroupDelta.getJSONArray("added");
		JSONArray sgDeletions = securityGroupDelta.getJSONArray("deleted");
		JSONArray sgModifications = securityGroupDelta.getJSONArray("modified");
		JSONArray ec2InstanceAdditions = ec2InstanceDelta.getJSONArray("added");
		JSONArray ec2InstanceDeletions = ec2InstanceDelta.getJSONArray("deleted");
		
		addSecurityGroups(sgAdditions, ec2Client);
		deleteSecurityGroups(sgDeletions, ec2Client);
		addEC2Instances(ec2InstanceAdditions, ec2Client);
		deleteEC2Instances(ec2InstanceDeletions, ec2Client);
		modifySecurityGroups(sgModifications, ec2Client);
		
		// RDS
		RDSClient rdsClient = new RDSClient();
		JSONObject rdsDelta = delta.getJSONObject("rds");
		JSONObject rdsInstanceDelta = rdsDelta.getJSONObject("instances");
		JSONArray rdsInstanceAdditions = rdsInstanceDelta.getJSONArray("added");
		JSONArray rdsInstanceDeletions = rdsInstanceDelta.getJSONArray("deleted");
		
		addRDSInstances(rdsInstanceAdditions, rdsClient);
		deleteRDSInstances(rdsInstanceDeletions, rdsClient);
		
		// Autoscaling
		AutoscaleClient autoscaleClient = new AutoscaleClient();
		JSONObject autoscaleDelta = delta.getJSONObject("autoscale");		
	}
	
	private void addRDSInstances(JSONArray instanceAdditions, RDSClient rdsClient) throws JSONException {
		for (int instanceIndex = 0; instanceIndex < instanceAdditions.length(); instanceIndex++) {
			JSONObject instance = instanceAdditions.getJSONObject(instanceIndex);
			String dbName = instance.getString("dbName");
			String engine = instance.getString("engine");
			String masterUsername = instance.getString("masterUsername");
			String masterUserPassword = instance.getString("masterPassword");
			String dbInstanceId = instance.getString("instanceId");
			String dBInstanceClass = instance.getString("instanceClass");
			Integer allocatedStorage = instance.getInt("allocatedStorage");
			rdsClient.createDBInstance(dbName, engine, masterUsername, masterUserPassword, dbInstanceId, dBInstanceClass, allocatedStorage);
		}
	}
	
	private void deleteRDSInstances(JSONArray instanceDeletions, RDSClient rdsClient) throws JSONException {
		for (int instanceIndex = 0; instanceIndex < instanceDeletions.length(); instanceIndex++) {
			JSONObject instance = instanceDeletions.getJSONObject(instanceIndex);
			rdsClient.terminateDBInstance(instance.getString("instanceId"), true);
		}
	}
	
	private void addEC2Instances(JSONArray instanceAdditions, EC2Client ec2Client) throws JSONException {
		for (int instanceIndex = 0; instanceIndex < instanceAdditions.length(); instanceIndex++) {
			JSONObject instance = instanceAdditions.getJSONObject(instanceIndex);
			String amiId = instance.getString("imageId");
			String type = instance.getString("instanceType");
			String keyName = instance.getString("keyName");
			JSONArray sgNames = instance.getJSONArray("securityGroups");
			JSONObject instanceTags = instance.getJSONObject("tags");
			ArrayList<String> securityGroups = new ArrayList<String>(); 
			for (int sgNameIndex = 0; sgNameIndex < sgNames.length(); sgNameIndex++){
				JSONObject securityGroup = sgNames.getJSONObject(sgNameIndex);
				securityGroups.add(securityGroup.getString("groupName"));
			}
			Map<String, String> tags = new HashMap<String, String>();
			Iterator<String> keys = instanceTags.keys();
			while (keys.hasNext()) {
			    String key = keys.next();
			    String value = instanceTags.getString(key);
			    tags.put(key, value);
			}
			ec2Client.launchNewInstances(amiId, type, 1, keyName, securityGroups, tags);
		}
	}
	
	private void deleteEC2Instances(JSONArray instanceDeletions, EC2Client ec2Client) throws JSONException {
		for (int instanceIndex = 0; instanceIndex < instanceDeletions.length(); instanceIndex++) {
			JSONObject instance = instanceDeletions.getJSONObject(instanceIndex);
			ec2Client.terminateInstance(instance.getString("instanceId"));
		}
	}
	
	private void modifySecurityGroups(JSONArray sgModifications, EC2Client ec2Client) throws JSONException {
		for (int sgIndex = 0; sgIndex < sgModifications.length(); sgIndex++) {
			JSONObject securityGroupInfo = sgModifications.getJSONObject(sgIndex);
			String groupId = (String) securityGroupInfo.keys().next();
			JSONObject securityGroup = securityGroupInfo.getJSONObject(groupId);
			if (securityGroup.has("tags")) {
				// Update group name
				System.out.println("Has Tag Modification");
				JSONObject keysAdded = securityGroup.getJSONObject("tags").getJSONObject("added");
				JSONObject keysDeleted = securityGroup.getJSONObject("tags").getJSONObject("deleted");
				JSONObject keysModified = securityGroup.getJSONObject("tags").getJSONObject("modified");
				
				Iterator addedKeys = keysAdded.keys();
				while (addedKeys.hasNext()) {
					String key = (String) addedKeys.next();
					String value = keysAdded.getString(key);
					System.out.println("Adding Key " + key + " for value " + value);
				}
				
				Iterator deletedKeys = keysDeleted.keys();
				while (deletedKeys.hasNext()) {
					String key = (String) deletedKeys.next();
					String value = keysDeleted.getString(key);
					System.out.println("Delting Key " + key + " for value " + value);
				}
				
				Iterator modifiedKeys = keysModified.keys();
				while (modifiedKeys.hasNext()) {
					String key = (String) modifiedKeys.next();
					String value = keysModified.getString(key);
					System.out.println("Modifying Key " + key + " for value " + value);
				}
			}
		}
	}
	
	private void deleteSecurityGroups(JSONArray sgDeletions, EC2Client ec2Client) throws JSONException {
		for (int sgIndex = 0; sgIndex < sgDeletions.length(); sgIndex++) {
			JSONObject securityGroup = sgDeletions.getJSONObject(sgIndex);
			ec2Client.deleteSecurityGroup(securityGroup.getString("groupId"));
		}
	}
	
	private void addSecurityGroups(JSONArray sgAdditions, EC2Client ec2Client) throws JSONException {
		for (int sgIndex = 0; sgIndex < sgAdditions.length(); sgIndex++) {
			JSONObject securityGroup = sgAdditions.getJSONObject(sgIndex);
			String description = securityGroup.getString("groupName");
			if (securityGroup.has("groupDescription")) {
				description = securityGroup.getString("groupDescription");
			}
			String groupId = ec2Client.createSecurityGroup(securityGroup.getString("groupName"), description);
			
			// Create inbound permissions
			JSONArray inboundPermissions = securityGroup.getJSONArray("permissions");
			for (int permInIndex = 0; permInIndex < sgAdditions.length(); permInIndex++) {
				JSONObject permission = inboundPermissions.getJSONObject(permInIndex);
				Integer startPort = null;
				Integer endPort = null;
				if (!JSONObject.NULL.equals(permission.get("fromPort"))) {
					startPort = new Integer(permission.getString("fromPort"));
				}
				if (!JSONObject.NULL.equals(permission.get("toPort"))) {
					endPort = new Integer(permission.getString("toPort"));
				}
				String protocol = permission.getString("ipProtocol");
				List<String> ipRanges = new ArrayList<String>();
				JSONArray jsonIpRanges = permission.getJSONArray("ipRanges");
				for (int ipIndex = 0; ipIndex < sgAdditions.length(); ipIndex++) {
					String ipRange = jsonIpRanges.getString(ipIndex);
					ipRanges.add(ipRange);
				}
				
			    ec2Client.addInboundSecurityGroupIpPermission(groupId, ipRanges, protocol, startPort, endPort);
			}
			// Create outbound permissions
			JSONArray outboundPermissions = (JSONArray) securityGroup.get("permissionsEgress");
			for (int permOutIndex = 0; permOutIndex < sgAdditions.length(); permOutIndex++) {
				JSONObject permission = outboundPermissions.getJSONObject(permOutIndex);
				Integer startPort = null;
				Integer endPort = null;
				if (!JSONObject.NULL.equals(permission.get("fromPort"))) {
					startPort = new Integer(permission.getString("fromPort"));
				}
				if (!JSONObject.NULL.equals(permission.get("toPort"))) {
					endPort = new Integer(permission.getString("toPort"));
				}
				String protocol = permission.getString("ipProtocol");
				List<String> ipRanges = new ArrayList<String>();
				JSONArray jsonIpRanges = permission.getJSONArray("ipRanges");
				for (int ipIndex = 0; ipIndex < sgAdditions.length(); ipIndex++) {
					String ipRange = jsonIpRanges.getString(ipIndex);
					ipRanges.add(ipRange);
				}
			    ec2Client.addOutboundSecurityGroupIpPermission(groupId, ipRanges, protocol, startPort, endPort);
			}
		}
	}
}
