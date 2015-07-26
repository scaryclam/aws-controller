package uk.co.bluesuntech.delta;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;


public class RDSInstanceDelta {
	public JSONObject getInstancesDelta(JSONObject currentConfig, JSONObject existingConfig) throws JSONException {
		JSONObject rdsDelta = new JSONObject();
		
		JSONObject currentRDSConfig = currentConfig.getJSONObject("rds");
        JSONArray currentInstance = currentRDSConfig.getJSONArray("instances");
        
        JSONObject existingRDSConfig = existingConfig.getJSONObject("rds");
        JSONArray existingInstances = existingRDSConfig.getJSONArray("instances");
        
        // Get deleted instances
        List<JSONObject> deletedInstances = getDeletedInstances(currentInstance, existingInstances);
        
        // Get new instances
        List<JSONObject> addedInstances = getAddedInstances(currentInstance, existingInstances);
        
        // TODO: Get modifications
        
        JSONObject instanceDelta = new JSONObject();
        instanceDelta.put("added", addedInstances);
        instanceDelta.put("deleted", deletedInstances);

        return instanceDelta;
	}
	
	public List<JSONObject> getAddedInstances(JSONArray currentRDSInstances, JSONArray existingRDSInstances) throws JSONException {
        List<JSONObject> addedInstances = new ArrayList<JSONObject>();
        
        List<String> existingInstances = new ArrayList<String>();
        for (int i = 0; i < existingRDSInstances.length(); i++) {
            JSONObject instance = existingRDSInstances.getJSONObject(i);
            existingInstances.add(instance.getString("endPoint"));
        }
        
        for (int i = 0; i < currentRDSInstances.length(); i++) {
            JSONObject instance = currentRDSInstances.getJSONObject(i);
            if (!existingInstances.contains(instance.get("endPoint"))) {
                addedInstances.add(instance);
            }
        }
        
        return addedInstances;
    }
    
    public List<JSONObject> getDeletedInstances(JSONArray currentRDSInstances, JSONArray existingRDSInstances) throws JSONException {
        List<JSONObject> deletedInstances = new ArrayList<JSONObject>();
        
        List<String> currentInstances = new ArrayList<String>();        
        for (int i = 0; i < currentRDSInstances.length(); i++) {
            JSONObject instance = currentRDSInstances.getJSONObject(i);
            currentInstances.add(instance.getString("endPoint"));
        }
        
        for (int i = 0; i < existingRDSInstances.length(); i++) {
            JSONObject instance = existingRDSInstances.getJSONObject(i);
            if (!currentInstances.contains(instance.getString("endPoint"))) {
                deletedInstances.add(instance);
            }
        }
        
        return deletedInstances;
    }
}
