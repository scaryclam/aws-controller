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

public class LoadBalancerClient {
	private AmazonElasticLoadBalancing lbClient;
	public LoadBalancerClient() {
		lbClient = new AmazonElasticLoadBalancingClient();
		Region region = Region.getRegion(Regions.EU_WEST_1);
	    lbClient.setRegion(region);
	}
	
	public void createLoadBalancer(String loadBalancerName, Collection<String> securityGroups, Collection<Listener> listeners, Collection<String> availabilityZones) {
		CreateLoadBalancerRequest request = new CreateLoadBalancerRequest();
		request.setLoadBalancerName(loadBalancerName);
		request.setSecurityGroups(securityGroups);
		request.setListeners(listeners);
		request.setAvailabilityZones(availabilityZones);
		System.out.println("Creating Load Balancer");
		CreateLoadBalancerResult result = lbClient.createLoadBalancer(request);
		System.out.println("Created Load Balancer");
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
