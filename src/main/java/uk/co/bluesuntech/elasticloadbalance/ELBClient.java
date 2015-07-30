package uk.co.bluesuntech.elasticloadbalance;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerRequest;


public class ELBClient {
    private AmazonElasticLoadBalancingClient elbClient;
    
    public ELBClient() {
        AWSCredentials credentials = new ProfileCredentialsProvider().getCredentials();
        elbClient = new AmazonElasticLoadBalancingClient(credentials);
        Region region = Region.getRegion(Regions.EU_WEST_1);
        elbClient.setRegion(region);
        System.out.println("Credentials Loaded");
    }
    
    public void createLoadBalancer() {
        CreateLoadBalancerRequest request = new CreateLoadBalancerRequest();
        elbClient.createLoadBalancer(request);
    }
}
