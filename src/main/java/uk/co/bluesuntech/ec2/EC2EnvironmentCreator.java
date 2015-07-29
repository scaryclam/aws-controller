package uk.co.bluesuntech.ec2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class EC2EnvironmentCreator {
    EC2Client ec2Client;
    
    public EC2EnvironmentCreator() {
        ec2Client = new EC2Client();
    }
    
    public EC2EnvironmentCreator(EC2Client inputEC2Client) {
        ec2Client = inputEC2Client;
    }
    
    public void createEnvFromConfig(JSONObject config) throws JSONException {
        createSecurityGroups(config);
        createInstances(config);
    }
    
    private void createInstances(JSONObject config) throws JSONException {
        JSONArray instances = config.getJSONArray("instances");
        for (int index = 0; index < instances.length(); index++) {
            JSONObject instance = instances.getJSONObject(index);
            String amiId = instance.getString("imageId");
            String type = instance.getString("instanceType"); 
            Integer number = 1;
            String keyName = instance.getString("keyName");
            JSONArray securityGroups = instance.getJSONArray("securityGroups");
            List<String> sgNames = new ArrayList<String>();
            for (int i = 0; i < securityGroups.length(); i++) {
                sgNames.add(securityGroups.getString(i));
            }
            Map<String, String> tags = new HashMap<String, String>();
            ec2Client.launchNewInstances(amiId, type, number, keyName, sgNames, tags);
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
                List<String> ipRanges = new ArrayList<String>();
                for (int i = 0; i < ranges.length(); i++) {
                    ipRanges.add(ranges.getString(i));
                }
                String protocol = rule.getString("ipProtocol");
                Integer startPort = rule.getInt("fromPort");
                Integer endPort = rule.getInt("toPort");
                ec2Client.addOutboundSecurityGroupIpPermission(sgId, ipRanges, protocol, startPort, endPort);
            }
        }
                    
    }
}
