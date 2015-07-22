package uk.co.bluesuntech.delta;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class EC2InstanceDelta {
    public JSONObject getInstancesDelta(JSONObject currentConfig, JSONObject existingConfig) throws JSONException {
        
        JSONObject currentEc2Config = (JSONObject) currentConfig.get("ec2");
        JSONArray currentInstance = (JSONArray) currentEc2Config.get("instances");
        
        JSONObject existingEc2Config = (JSONObject) existingConfig.get("ec2");
        JSONArray existingInstances = (JSONArray) existingEc2Config.get("instances");
        
        // Get deleted SGs
        List<JSONObject> deletedInstances = getDeletedInstances(currentInstance, existingInstances);
        
        // Get new SGs
        List<JSONObject> addedInstances = getAddedInstances(currentInstance, existingInstances);
        
        // TODO: Get modifications
        
        JSONObject sgDelta = new JSONObject();
        sgDelta.put("added", addedInstances);
        sgDelta.put("deleted", deletedInstances);

        return sgDelta;
    }

    public List<JSONObject> getAddedInstances(JSONArray currentEC2Instances, JSONArray existingEC2Instances) throws JSONException {
        List<JSONObject> addedInstances = new ArrayList<JSONObject>();
        
        List<String> existingInstances = new ArrayList<String>();
        for (int i = 0; i < existingEC2Instances.length(); i++) {
            JSONObject instance = existingEC2Instances.getJSONObject(i);
            existingInstances.add(instance.getString("instanceId"));
        }
        
        for (int i = 0; i < currentEC2Instances.length(); i++) {
            JSONObject instance = currentEC2Instances.getJSONObject(i);
            if (!existingInstances.contains(instance.get("instanceId"))) {
                addedInstances.add(instance);
            }
        }
        
        return addedInstances;
    }
    
    public List<JSONObject> getDeletedInstances(JSONArray currentEC2Instances, JSONArray existingEC2Instances) throws JSONException {
        List<JSONObject> deletedInstances = new ArrayList<JSONObject>();
        
        List<String> currentInstances = new ArrayList<String>();        
        for (int i = 0; i < currentEC2Instances.length(); i++) {
            JSONObject instance = currentEC2Instances.getJSONObject(i);
            currentInstances.add(instance.getString("instanceId"));
        }
        
        for (int i = 0; i < existingEC2Instances.length(); i++) {
            JSONObject instance = existingEC2Instances.getJSONObject(i);
            if (!currentInstances.contains(instance.get("instanceId"))) {
                deletedInstances.add(instance);
            }
        }
        
        return deletedInstances;
    }
}
