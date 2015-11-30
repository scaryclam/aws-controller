package uk.co.bluesunlabs.ec2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class EC2EnvironmentManager {
    EC2Client ec2Client;
    
    public EC2EnvironmentManager() {
        ec2Client = new EC2Client();
    }
    
    public EC2EnvironmentManager(EC2Client inputEC2Client) {
        ec2Client = inputEC2Client;
    }
    
    public void createEnvFromConfig(JSONObject config) throws JSONException {
    	if (config.has("securityGroups")) {
    		createSecurityGroups(config);
    		// Instances will need the security new group IDs
    		updateInstanceSecurityGroups(config);
    	}
        createInstances(config);
    }
    
    public void tearDownEnvFromConfig(JSONObject config) throws JSONException {
    	destroyInstances(config);
    }
    
    private void destroyInstances(JSONObject config) throws JSONException {
    	JSONArray instances = config.getJSONArray("instances");
        for (int index = 0; index < instances.length(); index++) {
            JSONObject instance = instances.getJSONObject(index);
            if (instance.has("instanceId")) {
           		ec2Client.terminateInstance(instance.getString("instanceId"));
            } else {
            	Instance instanceObj = ec2Client.findInstanceByControllerId(
           			instance.getJSONObject("tags").getString("controllerId"));
            	ec2Client.terminateInstance(instanceObj.getInstanceId());
            }
        }
    }
    
    private void updateInstanceSecurityGroups(JSONObject config) throws JSONException {
        JSONArray instances = config.getJSONArray("instances");
        JSONArray securityGroups = config.getJSONArray("securityGroups");
        Map<String, String> sgNames = new HashMap<String, String>();
        
        for (int index = 0; index < securityGroups.length(); index++) {
            JSONObject securityGroup = securityGroups.getJSONObject(index);
            sgNames.put(securityGroup.getString("groupName"), securityGroup.getString("groupId"));
        }
        
        for (int index = 0; index < instances.length(); index++) {
            JSONObject instance = instances.getJSONObject(index);
            // TODO; get the security groups for each instance and update them with the correct ID
            JSONArray instanceSecurityGroups = instance.getJSONArray("securityGroups");
            for (int i = 0; i < instanceSecurityGroups.length(); i++) {
                JSONObject instanceGroup = instanceSecurityGroups.getJSONObject(i);
                if (!sgNames.containsKey(instanceGroup.get("groupName"))) {
                	continue;
                }
                instanceGroup.put("groupId", sgNames.get(instanceGroup.get("groupName")));
            }
        }
    }
    
    private void createInstances(JSONObject config) throws JSONException {
        JSONArray instances = config.getJSONArray("instances");
        for (int index = 0; index < instances.length(); index++) {
            JSONObject instance = instances.getJSONObject(index);
            String amiId = instance.getString("imageId");
            String type = instance.getString("instanceType"); 
            Integer number = 1;
            String keyName = instance.getString("keyName");
            String userData = instance.getString("userData");
            JSONArray securityGroups = instance.getJSONArray("securityGroups");
            List<String> sgNames = new ArrayList<String>();
            for (int i = 0; i < securityGroups.length(); i++) {
                JSONObject sg = securityGroups.getJSONObject(i); 
                sgNames.add(sg.getString("groupId"));
            }
            Map<String, String> tags = new HashMap<String, String>();
            JSONObject instanceTags = instance.getJSONObject("tags");
            Iterator<String> keys = instanceTags.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                tags.put(key, instanceTags.getString(key));
            }
            ec2Client.launchNewInstances(amiId, type, number, keyName, sgNames, tags, userData);
        }
    }
    
    private void createSecurityGroups(JSONObject config) throws JSONException {
        JSONArray securityGroups = config.getJSONArray("securityGroups");
//        "groupId": "sg-983f28fd",
//        "groupName": "launch-wizard-1",
//        "permissions": [{
//        "fromPort": 22,
//        "ipProtocol": "tcp",
//        "ipRanges": ["0.0.0.0/0"],
//        "prefixListIds": [],
//        "toPort": 22,
//         "userIdGroupPairs": []
        for (int index = 0; index < securityGroups.length(); index++) {
            JSONObject securityGroup = securityGroups.getJSONObject(index);
            String sgId = ec2Client.createSecurityGroup(securityGroup.getString("groupName"), securityGroup.getString("groupDescription"));
            securityGroup.put("groupId", sgId);
            
            // Add the rules
            JSONArray ingressRules = securityGroup.getJSONArray("permissions");
            JSONArray egressRules = securityGroup.getJSONArray("permissionsEgress");
            
            for (int ruleIndex = 0; ruleIndex < ingressRules.length(); ruleIndex++) {
                JSONObject rule = ingressRules.getJSONObject(ruleIndex);
                JSONArray ranges = rule.getJSONArray("ipRanges");
                List<String> ipRanges = new ArrayList<String>();
                for (int i = 0; i < ranges.length(); i++) {
                    ipRanges.add(ranges.getString(i));
                }
                String protocol = rule.getString("ipProtocol");
                Integer startPort = rule.getInt("fromPort");
                Integer endPort = rule.getInt("toPort");
                ec2Client.addInboundSecurityGroupIpPermission(sgId, ipRanges, protocol, startPort, endPort);
            }
            
            for (int ruleIndex = 0; ruleIndex < egressRules.length(); ruleIndex++) {
                JSONObject rule = egressRules.getJSONObject(ruleIndex);
                JSONArray ranges = rule.getJSONArray("ipRanges");
                Integer startPort = 0;
                Integer endPort = 0;
                List<String> ipRanges = new ArrayList<String>();
                for (int i = 0; i < ranges.length(); i++) {
                    ipRanges.add(ranges.getString(i));
                }
                String protocol = rule.getString("ipProtocol");
                try {
                    startPort = rule.getInt("fromPort");
                } catch (JSONException error) {
                    ec2Client.addOutboundSecurityGroupIpPermission(sgId, ipRanges, protocol);
                }
                try {
                    endPort = rule.getInt("toPort");
                } catch (JSONException error) {
                    ec2Client.addOutboundSecurityGroupIpPermission(sgId, ipRanges, protocol);
                }
                
                ec2Client.addOutboundSecurityGroupIpPermission(sgId, ipRanges, protocol, startPort, endPort);
            }
        }
                    
    }
}
