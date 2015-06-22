package uk.co.bluesuntech.autoscaling;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;

public class AutoscaleClient {
	
	private AmazonAutoScaling asClient;
	
	public AutoscaleClient() {
		asClient = new AmazonAutoScalingClient();
		Region region = Region.getRegion(Regions.EU_WEST_1);
		asClient.setRegion(region);
	}
}
