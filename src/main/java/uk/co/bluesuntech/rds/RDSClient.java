package uk.co.bluesuntech.rds;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.rds.model.CreateDBInstanceRequest;
import com.amazonaws.services.rds.model.DeleteDBInstanceRequest;


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
}
