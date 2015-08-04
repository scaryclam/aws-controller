package uk.co.bluesuntech.rds;


import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;


public class RDSEnvironmentCreator {
    RDSClient rdsClient;
    
    public RDSEnvironmentCreator() {
        rdsClient = new RDSClient();
    }
    
    public RDSEnvironmentCreator(RDSClient inputRDSClient) {
        rdsClient = inputRDSClient;
    }
    
    public void createEnvFromConfig(JSONObject config) throws JSONException {
        createRDSInstances(config);
    }
    
    private void createRDSInstances(JSONObject config) throws JSONException {
        JSONArray instances = config.getJSONArray("instances");
        for (int index = 0; index < instances.length(); index++) {
            JSONObject instance = instances.getJSONObject(index);
            String dbName = instance.getString("dbName");
            String engine = instance.getString("engine");
            String masterUsername = instance.getString("masterUsername");
            String dbInstanceId = instance.getString("instanceId");
            String masterUserPassword = instance.getString("masterUserPassword");
            String dBInstanceClass = instance.getString("instanceClass");
            Integer allocatedStorage = instance.getInt("allocatedStorage");
            
            rdsClient.createDBInstance(dbName, engine, masterUsername, masterUserPassword, dbInstanceId, dBInstanceClass, allocatedStorage);
        }
    }

}
