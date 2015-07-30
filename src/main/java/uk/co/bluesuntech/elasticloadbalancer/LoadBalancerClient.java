package uk.co.bluesuntech.elasticloadbalancer;

import java.util.Collection;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerListenersRequest;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerResult;
import com.amazonaws.services.elasticloadbalancing.model.Listener;
import com.amazonaws.services.elasticloadbalancing.model.Tag;


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
	
//	public void addListenersToLB(Collection<Listener> listeners) {
//		CreateLoadBalancerListenersRequest request = new CreateLoadBalancerListenersRequest();
//		request.setListeners(listeners);
//	}
}
