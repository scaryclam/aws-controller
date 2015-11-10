package uk.co.bluesunlabs.elasticloadbalancer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerListenersRequest;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerResult;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.Listener;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import com.amazonaws.services.elasticloadbalancing.model.Tag;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;


public class LoadBalancerClient {
	private AmazonElasticLoadBalancing lbClient;
	
	public LoadBalancerClient() {
		lbClient = new AmazonElasticLoadBalancingClient();
		Region region = Region.getRegion(Regions.EU_WEST_1);
	    lbClient.setRegion(region);
	}
	
	public CreateLoadBalancerResult createLoadBalancer(String loadBalancerName, Collection<String> securityGroups, Collection<Listener> listeners, Collection<String> availabilityZones, Collection<Tag> tags) {
		CreateLoadBalancerRequest request = new CreateLoadBalancerRequest();
		request.withLoadBalancerName(loadBalancerName)
		       .withSecurityGroups(securityGroups)
		       .withListeners(listeners)
		       .withAvailabilityZones(availabilityZones)
		       .withTags(tags);
		System.out.println("Creating Load Balancer");
		CreateLoadBalancerResult result = lbClient.createLoadBalancer(request);
		System.out.println("Created Load Balancer");
		return result;
	}
	
	public Listener createListener(String protocol, Integer lbPort, Integer instancePort) {
		Listener listener = new Listener(protocol, lbPort, instancePort);
		return listener;
	}
	
	private List<LoadBalancerDescription> getLoadBalancers() {
	    DescribeLoadBalancersResult result = lbClient.describeLoadBalancers();
	    return result.getLoadBalancerDescriptions();
	}
	
	// JSON Fetchers
	public List<JSONObject> getLoadBalancersAsJson() throws JSONException {
	    List<JSONObject> loadBalancers = new ArrayList<JSONObject>();
	    List<LoadBalancerDescription> elbs = getLoadBalancers();
	    for (LoadBalancerDescription elb : elbs) {
	        JSONObject currentElb = new JSONObject();
	        currentElb.put("availabilityZones", elb.getAvailabilityZones());
	        currentElb.put("backendServerDescriptions", elb.getBackendServerDescriptions());
            currentElb.put("canonicalHostedZoneName", elb.getCanonicalHostedZoneName());
            currentElb.put("canonicalHostedZoneNameId", elb.getCanonicalHostedZoneNameID());
            currentElb.put("createdTime", elb.getCreatedTime());
            currentElb.put("dnsName", elb.getDNSName());
            currentElb.put("healthCheck", elb.getHealthCheck());
            currentElb.put("instances", elb.getInstances());
            currentElb.put("listenerDescriptions", elb.getListenerDescriptions());
            currentElb.put("name", elb.getLoadBalancerName());
            currentElb.put("policies", elb.getPolicies());
            currentElb.put("scheme", elb.getScheme());
            currentElb.put("securityGroups", elb.getSecurityGroups());
            currentElb.put("sourceSecurityGroup", elb.getSourceSecurityGroup());
            currentElb.put("subnets", elb.getSubnets());
            currentElb.put("vpcId", elb.getVPCId());
            loadBalancers.add(currentElb);
	    }
	    return loadBalancers;
	}
	
//	public void addListenersToLB(Collection<Listener> listeners) {
//		CreateLoadBalancerListenersRequest request = new CreateLoadBalancerListenersRequest();
//		request.setListeners(listeners);
//	}
}
