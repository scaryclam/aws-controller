package uk.co.bluesuntech.ec2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.autoscaling.model.Filter;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerResult;
import com.amazonaws.services.opsworks.model.StartInstanceRequest;
import com.amazonaws.services.opsworks.model.StopInstanceRequest;


public class EC2Client {
	private AmazonEC2 ec2Client;
	private HashMap<String, Instance> instances;
	
	public EC2Client() {
		System.out.println("Loading ec2 credentials...");
		AWSCredentials credentials = new ProfileCredentialsProvider().getCredentials();
		ec2Client = new AmazonEC2Client(credentials);
	    Region region = Region.getRegion(Regions.EU_WEST_1);
	    ec2Client.setRegion(region);
		System.out.println("Credentials Loaded");
	}
	
	public void getAllInstances() {
	    DescribeInstancesResult results = ec2Client.describeInstances();
	    
	    List<Reservation> reservations = results.getReservations();
	    for (Reservation reservation : reservations) {
	    	List<Instance> runningInstances = reservation.getInstances();
	    	
	    	if (instances == null) {
	    		instances = new HashMap<String, Instance>();
	    	}
	    	
	    	for (Instance instance : runningInstances) {
	    		instances.put(instance.getInstanceId(), instance);
	    	}
	    }
	}
	
	public void showAllInstances() {
		System.out.println("These are your instances:");
		for (Instance instance : instances.values()) {
			System.out.println(instance);
		}
	}
	
	public HashMap<String, Instance> getKnownInstances() {
		return instances;
	}
	
	public Collection<Instance> launchNewInstances(String AMI_ID, String type, Integer number) {
		RunInstancesRequest request = this.getRunInstanceRequest(AMI_ID, type, number);
		RunInstancesResult result = launchInstances(request);
		return result.getReservation().getInstances();
	}
	
	public Collection<Instance> launchNewInstances(String AMI_ID, String type, Integer number, String keyName) {
		RunInstancesRequest request = this.getRunInstanceRequest(AMI_ID, type, number);
		request.setKeyName(keyName);
		RunInstancesResult result = launchInstances(request);
		return result.getReservation().getInstances();
	}
	
	public Collection<Instance> launchNewInstances(String AMI_ID, String type, Integer number, String keyName, Collection<String> sgNames) {
		RunInstancesRequest request = this.getRunInstanceRequest(AMI_ID, type, number);
		request.setKeyName(keyName);
		request.setSecurityGroupIds(sgNames);
		RunInstancesResult result = launchInstances(request);
		return result.getReservation().getInstances();
	}
	
	public void addTagsToInstance(Instance instance, Map<String, String> tags) {
		CreateTagsRequest request = new CreateTagsRequest();
		Collection<Tag> requestTags = new ArrayList<Tag>();
		
		for (Entry<String, String> tagEntry : tags.entrySet()) {
			Tag tag = new Tag(tagEntry.getKey(), tagEntry.getValue());
			requestTags.add(tag);
		}
		
		request.withResources(instance.getInstanceId());
		request.setTags(requestTags);
		
		ec2Client.createTags(request);
	}
	
	private RunInstancesResult launchInstances(RunInstancesRequest request) {
		System.out.println("Launching Instance");
		RunInstancesResult result = ec2Client.runInstances(request);
		System.out.println("Launched Instance");
		for (Instance instance : result.getReservation().getInstances()) {
			instances.put(instance.getInstanceId(), instance);
		}
		return result;
	}
	
	public void stopInstance(String instanceId) {
		List<String> instanceIds = new ArrayList<String>();
		instanceIds.add(instanceId);
		StopInstancesRequest request = new StopInstancesRequest();
		request.setInstanceIds(instanceIds);
		System.out.println("Stopping Instance");
		StopInstancesResult result = ec2Client.stopInstances(request);
		System.out.println("Stopped Instance");
	}
	
	public void terminateInstance(String instanceId) {
		List<String> instanceIds = new ArrayList<String>();
		instanceIds.add(instanceId);
		TerminateInstancesRequest request = new TerminateInstancesRequest(instanceIds);
		System.out.println("Terminating Instance");
		ec2Client.terminateInstances(request);
		System.out.println("Terminated Instance");
		instances.remove(instanceId);
	}
	
	public void startInstance(String instanceId) {
		List<String> instanceIds = new ArrayList<String>();
		instanceIds.add(instanceId);
		StartInstancesRequest request = new StartInstancesRequest(instanceIds);
		System.out.println("Starting Instance");
		StartInstancesResult result = ec2Client.startInstances(request);
		System.out.println("Started Instance");
	}
	
	private RunInstancesRequest getRunInstanceRequest(String AMI_ID, String type, Integer number) {
		RunInstancesRequest request = new RunInstancesRequest(AMI_ID, number, number);
		request.setInstanceType(type);
		return request;
	}
	
	public void createSecurityGroup(String groupName, String groupDescription) {
		CreateSecurityGroupRequest request = new CreateSecurityGroupRequest();
		request.setGroupName(groupName);
		request.setDescription(groupDescription);
		System.out.println("Creating security group");
		CreateSecurityGroupResult result = ec2Client.createSecurityGroup(request);
		System.out.println("Created security group");
	}
	
	public void addSecurityGroupIpPermission(String sgName, Collection<String> ipRanges, String protocol, Integer startPort, Integer endPort) {
		IpPermission ipPermission = new IpPermission();
			    	
		ipPermission.withIpRanges(ipRanges)
			        .withIpProtocol(protocol)
			        .withFromPort(startPort)
			        .withToPort(endPort);
		AuthorizeSecurityGroupIngressRequest request = new AuthorizeSecurityGroupIngressRequest();
			    	
		request.withGroupName(sgName)
		       .withIpPermissions(ipPermission);
	
		ec2Client.authorizeSecurityGroupIngress(request);
	}
}
