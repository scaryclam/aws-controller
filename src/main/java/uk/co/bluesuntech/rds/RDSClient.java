package uk.co.bluesuntech.rds;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.rds.model.CreateDBInstanceRequest;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DeleteDBInstanceRequest;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;


public class RDSClient {
	private AmazonRDS rdsClient;
	
	public RDSClient() {
		rdsClient = new AmazonRDSClient();
		Region region = Region.getRegion(Regions.EU_WEST_1);
		rdsClient.setRegion(region);
	}
	
	public void createDBInstance(String dbName, String engine, String masterUsername, String masterUserPassword, String dbInstanceId, String dBInstanceClass, Integer allocatedStorage) {
		CreateDBInstanceRequest request = new CreateDBInstanceRequest();
		request.setDBName(dbName);
		request.setEngine(engine);
		request.setMasterUsername(masterUsername);
		request.setMasterUserPassword(masterUserPassword);
		request.setDBInstanceIdentifier(dbInstanceId);
		request.setDBInstanceClass(dBInstanceClass);
		request.setAllocatedStorage(allocatedStorage);
		System.out.println("Creating Database Instance");
		rdsClient.createDBInstance(request);
		System.out.println("Created Database Instance");
	}
	
	public void terminateDBInstance(String dBInstanceIdentifier, Boolean skipFinalSnapshot) {
		DeleteDBInstanceRequest request = new DeleteDBInstanceRequest(dBInstanceIdentifier);
		request.setSkipFinalSnapshot(skipFinalSnapshot);
		System.out.println("Deleting DB Instance");
	    rdsClient.deleteDBInstance(request);
	    System.out.println("Deleted DB Instance");
	}
	
	private List<DBInstance> getRdsInstances() {
		DescribeDBInstancesResult results = rdsClient.describeDBInstances();
		return results.getDBInstances();
	}
	
	// JSON Fetchers
	public List<JSONObject> getRDSInstancesAsJson() throws JSONException {
		List<JSONObject> rdsInstances = new ArrayList<JSONObject>();
		List<DBInstance> instances = getRdsInstances();
		for (DBInstance instance : instances) {
			JSONObject currentInstance = new JSONObject();
			currentInstance.put("instanceId", instance.getDBInstanceIdentifier());
			currentInstance.put("endPoint", instance.getEndpoint());
			currentInstance.put("dbName", instance.getDBName());
			currentInstance.put("masterUsername", instance.getMasterUsername());
			currentInstance.put("engine", instance.getEngine());
			currentInstance.put("engineVersion", instance.getEngineVersion());
			currentInstance.put("allocatedStorage", instance.getAllocatedStorage());
			currentInstance.put("instanceClass", instance.getDBInstanceClass());
			currentInstance.put("status", instance.getDBInstanceStatus());
			currentInstance.put("endPoint", instance.getEndpoint());
			currentInstance.put("availabilityZone", instance.getAvailabilityZone());
			currentInstance.put("multiAZ", instance.getMultiAZ());
			currentInstance.put("securityGroups", instance.getVpcSecurityGroups());
			rdsInstances.add(currentInstance);
		}
		return rdsInstances;
	}
}
