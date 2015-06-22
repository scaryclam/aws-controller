package uk.co.bluesuntech.autoscaling;

import java.util.Collection;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.CreateLaunchConfigurationRequest;
import com.amazonaws.services.autoscaling.model.InstanceMonitoring;

public class AutoscaleClient {
	
	private AmazonAutoScaling asClient;
	
	public AutoscaleClient() {
		asClient = new AmazonAutoScalingClient();
		Region region = Region.getRegion(Regions.EU_WEST_1);
		asClient.setRegion(region);
	}
	
	public void createLaunchConfiguration(String launchConfigName, String imageId, String instanceType, Collection<String> securityGroups, InstanceMonitoring instanceMonitoring) {
		CreateLaunchConfigurationRequest lcRequest = new CreateLaunchConfigurationRequest();
        lcRequest.setLaunchConfigurationName(launchConfigName);
        lcRequest.setImageId(imageId);
        lcRequest.setInstanceType(instanceType);
        lcRequest.setSecurityGroups(securityGroups);
        lcRequest.setInstanceMonitoring(instanceMonitoring);
        asClient.createLaunchConfiguration(lcRequest);
	}
}
