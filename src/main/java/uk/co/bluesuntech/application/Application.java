package uk.co.bluesuntech.application;

import java.awt.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.Listener;

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
	// 5) Create load balancers - can do
	// 6) Create autoscaling group - todo
	// 7) Modify autoscaling group - todo
	
	public static void main(String[] args) {
		EC2Client ec2Client = new EC2Client();
		ec2Client.getAllInstances();
		ec2Client.showAllInstances();
		Map<String, String> tags = new HashMap<String, String>();
		tags.put("Name", "test-instance");
		Collection<String> groups = new ArrayList<String>();
		groups.add("sg-da576fbf");
		
		//Collection<Instance> newInstances = ec2Client.launchNewInstances("ami-47a23a30", "t2.micro", 1, "tesy-key-2", groups);
		//for (Instance instance : newInstances) {
		//	ec2Client.addTagsToInstance(instance, tags);
		//}
		
//		ec2Client.startInstance("i-19684db3");
//		ec2Client.terminateInstance("i-93745139");
//		ec2Client.createSecurityGroup("testSG2", "Test SG 2");
		Collection<String> ips = new ArrayList<String>();
		ips.add("82.2.84.131/32");
		ec2Client.addSecurityGroupIpPermission("testSG2", ips, "tcp", 22, 22);
		
		RDSClient rdsClient = new RDSClient();
//		rdsClient.createDBInstance("testDB", "postgres", "testUser", "wibble123", "TestDB", "db.t2.micro", 8);
//		rdsClient.terminateDBInstance("TestDB", true);
		
		LoadBalancerClient lbClient = new LoadBalancerClient();
		
		Collection<String> availabilityZones = new ArrayList<String>();
		availabilityZones.add("eu-west-1a");
		availabilityZones.add("eu-west-1b");
		availabilityZones.add("eu-west-1c");
		
		Collection<Listener> listeners = new ArrayList<Listener>();
		//Listener listener = lbClient.createListener("http", 80, 80);
		//listeners.add(listener);
		//lbClient.createLoadBalancer("test-balancer-2", groups, listeners, availabilityZones);
	}
}
