package uk.co.bluesuntech.delta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.co.bluesuntech.ec2.EC2Client;

import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class DeltaApplier {
	public void applyDelta(JSONObject delta) throws JSONException {
		System.out.println("Applying Changes");
		JSONObject ec2Delta = delta.getJSONObject("ec2");
		JSONObject securityGroupDelta = (JSONObject) ec2Delta.get("securityGroups");
		
		// Add Security Groups
		JSONArray sgAdditions = (JSONArray) securityGroupDelta.get("added");
		JSONArray sgDeletions = (JSONArray) securityGroupDelta.get("deleted");
		JSONArray sgModifications = (JSONArray) securityGroupDelta.get("modified");
		EC2Client ec2Client = new EC2Client();
		
		addSecurityGroups(sgAdditions, ec2Client);
		deleteSecurityGroups(sgDeletions, ec2Client);
		modifySecurityGroups(sgModifications, ec2Client);
		
	}
	
	private void modifySecurityGroups(JSONArray sgModifications, EC2Client ec2Client) throws JSONException {
		for (int sgIndex = 0; sgIndex < sgModifications.length(); sgIndex++) {
			JSONObject securityGroupInfo = sgModifications.getJSONObject(sgIndex);
			String groupId = (String) securityGroupInfo.keys().next();
			JSONObject securityGroup = securityGroupInfo.getJSONObject(groupId);
			if (securityGroup.has("groupName")) {
				// Update group name
				System.out.println("Has Group Name Modification");
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
