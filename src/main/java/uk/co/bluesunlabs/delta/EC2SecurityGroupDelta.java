package uk.co.bluesunlabs.delta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class EC2SecurityGroupDelta {
	public JSONObject getSecurityGroupDelta(JSONObject currentConfig, JSONObject existingConfig) throws JSONException {
		
		JSONObject currentEc2Config = (JSONObject) currentConfig.get("ec2");
		JSONArray currentSecurityGroups = (JSONArray) currentEc2Config.get("securityGroups");
		
		JSONObject existingEc2Config = (JSONObject) existingConfig.get("ec2");
		JSONArray existingSecurityGroups = (JSONArray) existingEc2Config.get("securityGroups");
		
		// Get deleted SGs
		List<JSONObject> deletedGroups = getDeletedSecurityGroups(currentSecurityGroups, existingSecurityGroups);
		
		// Get new SGs
		List<JSONObject> addedGroups = getNewSecurityGroups(currentSecurityGroups, existingSecurityGroups);
		
		// Get modifications
		List<JSONObject> modifiedGroups = getModifiedSecurityGroups(currentSecurityGroups, existingSecurityGroups);
		
		JSONObject sgDelta = new JSONObject();
		sgDelta.put("added", addedGroups);
		sgDelta.put("deleted", deletedGroups);
		sgDelta.put("modified", modifiedGroups);
		return sgDelta;
	}
	
	private List<JSONObject> getModifiedSecurityGroups(JSONArray currentSecurityGroups, JSONArray existingSecurityGroups) throws JSONException {
		List<JSONObject> modifications = new ArrayList<JSONObject>();
		for (int sgIndex = 0; sgIndex < existingSecurityGroups.length(); sgIndex++) {
			JSONObject securityGroup = existingSecurityGroups.getJSONObject(sgIndex);
			JSONObject workingGroup = null;
			// Get the same SG from the current groups
			for (int groupIndex = 0; groupIndex < currentSecurityGroups.length(); groupIndex++) {
				JSONObject currentGroup = currentSecurityGroups.getJSONObject(groupIndex);
				if (currentGroup.get("groupId").equals(securityGroup.get("groupId"))) {
					workingGroup = currentGroup;
					break;
				}
			}
			if (workingGroup == null) {
				// This SG is not in both groups. There is nothing to compare
				continue;
			}
			
			JSONObject SGDelta = new JSONObject();
			
			Boolean modified = false;
			JSONObject SGMods = new JSONObject();
			
			// Only the tags can be updated on AWS, so only get the differences for these
			
			JSONObject tagsDelta = getTagDelta(securityGroup, workingGroup);
			
			if (tagsDelta.getJSONObject("added").keys().hasNext() || tagsDelta.getJSONObject("deleted").keys().hasNext() || tagsDelta.getJSONObject("modified").keys().hasNext()) {
				modified = true;
			}
			SGMods.put("tags", tagsDelta);
			
			if (modified) {
				SGDelta.put((String) securityGroup.get("groupId"), SGMods);
			    modifications.add(SGDelta);
			}
		}
		
		return modifications;
	}
	
	private JSONObject getTagDelta(JSONObject securityGroup, JSONObject workingGroup) throws JSONException {
		// Check the tags
		JSONObject tagsDelta = new JSONObject();
		JSONObject tagMods = new JSONObject();
		JSONObject tagAdditions = new JSONObject();
		JSONObject tagDeletions = new JSONObject();
		
		JSONObject sgTags = (JSONObject) securityGroup.get("tags");
		Iterator<String> keys = sgTags.keys(); 
		
		while (keys.hasNext()) {				
			JSONObject workingTags = (JSONObject) workingGroup.get("tags");
			String key = keys.next();
			if (workingTags.has(key)) {
				// Check if key modified
				if (!workingTags.get(key).equals(sgTags.get(key))) {
					JSONObject tagDelta = new JSONObject();
					tagDelta.put("groupId", securityGroup.get("groupId"));
					tagDelta.put("old", sgTags.get(key));
					tagDelta.put("new", workingTags.get(key));
					tagMods.put(key, tagDelta);
				}
			} else {
				// There isn't key, so it must have been deleted
				tagDeletions.put(key, sgTags.get(key));
			}
		}
		
		// Now find the added tags
		JSONObject workingSgTags = (JSONObject) workingGroup.get("tags");
		Iterator<String> workingKeys = workingSgTags.keys();
		
		while (workingKeys.hasNext()) {
			JSONObject securityGroupTags = (JSONObject) securityGroup.get("tags");
			String key = workingKeys.next();
			if (!securityGroupTags.has(key)) {
				// The tag is new
				tagAdditions.put(key, workingSgTags.get(key));
			}
		}
		
		tagsDelta.put("modified", tagMods);
		tagsDelta.put("added", tagAdditions);
		tagsDelta.put("deleted", tagDeletions);
		
		return tagsDelta;
	}
	
	private List<JSONObject> getNewSecurityGroups(JSONArray currentSecurityGroups, JSONArray existingSecurityGroups) throws JSONException {
        List<JSONObject> addedGroups = new ArrayList<JSONObject>();
		
		List<String> existingGroups = new ArrayList<String>();
		for (int i = 0; i < existingSecurityGroups.length(); i++) {
			JSONObject securityGroup = existingSecurityGroups.getJSONObject(i);
			existingGroups.add(securityGroup.getString("groupId"));
		}
		
		for (int i = 0; i < currentSecurityGroups.length(); i++) {
			JSONObject securityGroup = currentSecurityGroups.getJSONObject(i);
			if (!existingGroups.contains(securityGroup.get("groupId"))) {
				addedGroups.add(securityGroup);
			}
		}
		
		return addedGroups;
	}
	
	private List<JSONObject> getDeletedSecurityGroups(JSONArray currentSecurityGroups, JSONArray existingSecurityGroups) throws JSONException {
		List<JSONObject> deletedGroups = new ArrayList<JSONObject>();
		
		List<String> currentGroups = new ArrayList<String>();
		for (int i = 0; i < currentSecurityGroups.length(); i++) {
			JSONObject securityGroup = currentSecurityGroups.getJSONObject(i);
			currentGroups.add(securityGroup.getString("groupId"));
		}
		
		for (int i = 0; i < existingSecurityGroups.length(); i++) {
			JSONObject securityGroup = existingSecurityGroups.getJSONObject(i);
			if (!currentGroups.contains(securityGroup.get("groupId"))) {
				deletedGroups.add(securityGroup);
			}
		}
		
		return deletedGroups;
	}
}
