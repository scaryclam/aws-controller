package uk.co.bluesunlabs.rds;


import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;


public class RDSEnvironmentManager {
    RDSClient rdsClient;
    
    public RDSEnvironmentManager() {
        rdsClient = new RDSClient();
    }
    
    public RDSEnvironmentManager(RDSClient inputRDSClient) {
        rdsClient = inputRDSClient;
    }
    
    public void createEnvFromConfig(JSONObject config) throws JSONException {
        createRDSInstances(config);
    }
    
    public void tearDownEnvFromConfig(JSONObject config) throws JSONException {
    	tearDownRDSInstances(config);
    }
    
    private void createRDSInstances(JSONObject config) throws JSONException {
        JSONArray instances = config.getJSONArray("instances");
        for (int index = 0; index < instances.length(); index++) {
            JSONObject instance = instances.getJSONObject(index);
            String dbName = instance.getString("dbName");
            String engine = instance.getString("engine");
            String masterUsername = instance.getString("masterUsername");
            String masterUserPassword = instance.getString("masterUserPassword");
            String dBInstanceClass = instance.getString("instanceClass");
            Integer allocatedStorage = null;
            try {
            	allocatedStorage = instance.getInt("allocatedStorage");
            } catch (JSONException error) {
            	allocatedStorage = Integer.parseInt(instance.getString("allocatedStorage"));
            }
            
            if (instance.has("instanceId")) {
            	String dbInstanceId = instance.getString("instanceId");
            	rdsClient.createDBInstance(
            			dbName, engine, masterUsername, masterUserPassword, 
            			dbInstanceId, dBInstanceClass, allocatedStorage);
            } else {
            	rdsClient.createDBInstance(
            			dbName, engine, masterUsername, masterUserPassword, 
            			dBInstanceClass, allocatedStorage);
            }
        }
    }
    
    private void tearDownRDSInstances(JSONObject config) throws JSONException {
    	JSONArray instances = config.getJSONArray("instances");
        for (int index = 0; index < instances.length(); index++) {
            JSONObject instance = instances.getJSONObject(index);
            String instanceId = instance.getString("instanceId");
            Boolean skipSnapshot = false;
            if (instance.has("skipFinalSnapshot")) {
            	try {
            		skipSnapshot = instance.getBoolean("skipFinalSnapshot");
            	} catch (JSONException error) {
            		String skipSnapshotStr = instance.getString("skipFinalSnapshot");
            		skipSnapshot = Boolean.valueOf(skipSnapshotStr);
            	}
            }
            rdsClient.terminateDBInstance(instanceId, skipSnapshot);
        }
    }

}
