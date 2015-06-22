package uk.co.bluesuntech.autoscaling;

import java.util.Collection;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.CreateAutoScalingGroupRequest;
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
		CreateLaunchConfigurationRequest request = new CreateLaunchConfigurationRequest();
        request.setLaunchConfigurationName(launchConfigName);
        request.setImageId(imageId);
        request.setInstanceType(instanceType);
        request.setSecurityGroups(securityGroups);
        request.setInstanceMonitoring(instanceMonitoring);
        asClient.createLaunchConfiguration(request);
	}
	
	public void createAutoScalingGroup(String autoScaleGroupName, String launchConfiguration, Collection<String> availabilityZones, Collection<String> loadBalancerNames, String healthCheckType, Integer healthCheckGracePeriod, Integer defaultCooldown) {
		CreateAutoScalingGroupRequest request = new CreateAutoScalingGroupRequest();
        request.setAutoScalingGroupName(autoScaleGroupName);
        request.setLaunchConfigurationName(launchConfiguration);
        request.setAvailabilityZones(availabilityZones);
        request.setLoadBalancerNames(loadBalancerNames);
        request.setHealthCheckType(healthCheckType);
        request.setHealthCheckGracePeriod(healthCheckGracePeriod);
        request.setDefaultCooldown(defaultCooldown);
        asClient.createAutoScalingGroup(request);
	}
}
