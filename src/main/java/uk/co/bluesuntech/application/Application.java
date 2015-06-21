package uk.co.bluesuntech.application;

import java.awt.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import uk.co.bluesuntech.ec2.EC2Client;
import uk.co.bluesuntech.elasticloadbalancer.LoadBalancerClient;
import uk.co.bluesuntech.rds.RDSClient;


public class Application {
	private static String AWS_ACCESS_KEY_ID;
	private static String AWS_SECRET_ACCESS_KEY_ID;
	
	// 1) Create security groups - can do
	// 2) Create instance profiles - todo
	// 3) Create instances - can do
	// 4) Create RDS instances - can do
	// 5) Create load balancers - todo
	// 6) Create autoscaling group - todo
	// 7) Modify autoscaling group - todo
	
	public static void main(String[] args) {
		EC2Client ec2Client = new EC2Client();
		ec2Client.getAllInstances();
		ec2Client.showAllInstances();
//		ec2Client.launchNewInstance("ami-47a23a30", "t2.micro", 1);
//		ec2Client.startInstance("i-19684db3");
//		ec2Client.terminateInstance("i-93745139");
//		ec2Client.createSecurityGroup("testSG2", "Test SG 2");
		
		RDSClient rdsClient = new RDSClient();
//		rdsClient.createDBInstance("testDB", "postgres", "testUser", "wibble123", "TestDB", "db.t2.micro", 8);
//		rdsClient.terminateDBInstance("TestDB", true);
		
		LoadBalancerClient lbClient = new LoadBalancerClient();
		Collection<String> groups = new ArrayList<String>();
		groups.add("testSG2");
		lbClient.createLoadBalancer("test-balancer-2", groups);
	}
}
