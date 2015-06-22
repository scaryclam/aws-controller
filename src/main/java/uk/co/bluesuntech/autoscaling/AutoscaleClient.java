package uk.co.bluesuntech.autoscaling;

import java.util.Collection;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.CreateAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.CreateLaunchConfigurationRequest;
import com.amazonaws.services.autoscaling.model.InstanceMonitoring;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyRequest;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyResult;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.ComparisonOperator;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.PutMetricAlarmRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.amazonaws.services.cloudwatch.model.Statistic;

public class AutoscaleClient {
	
	private AmazonAutoScaling asClient;
	private AmazonCloudWatch cwClient;
	
	public AutoscaleClient() {
		asClient = new AmazonAutoScalingClient();
		Region region = Region.getRegion(Regions.EU_WEST_1);
		asClient.setRegion(region);
		
		cwClient = new AmazonCloudWatchClient();
		cwClient.setRegion(region);
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
	
	public void createAutoScalingGroup(String autoScaleGroupName, String launchConfiguration, Collection<String> availabilityZones, Collection<String> loadBalancerNames, String healthCheckType, Integer healthCheckGracePeriod, Integer defaultCooldown, Integer minSize, Integer maxSize) {
		CreateAutoScalingGroupRequest request = new CreateAutoScalingGroupRequest();
        request.setAutoScalingGroupName(autoScaleGroupName);
        request.setLaunchConfigurationName(launchConfiguration);
        request.setAvailabilityZones(availabilityZones);
        request.setLoadBalancerNames(loadBalancerNames);
        request.setHealthCheckType(healthCheckType);
        request.setHealthCheckGracePeriod(healthCheckGracePeriod);
        request.setDefaultCooldown(defaultCooldown);
        request.setMinSize(minSize);
        request.setMaxSize(maxSize);
        asClient.createAutoScalingGroup(request);
	}
	
	public PutScalingPolicyResult createAutoScalingPolicy(String groupName, String policyName, Integer scalingAdjustment, String adjustmentType) {
		PutScalingPolicyRequest request = new PutScalingPolicyRequest();
        request.setAutoScalingGroupName(groupName);
        request.setPolicyName(policyName);
        request.setScalingAdjustment(scalingAdjustment);
        request.setAdjustmentType(adjustmentType);

        PutScalingPolicyResult result = asClient.putScalingPolicy(request);
        return result;
	}
	
	public void createScaleUpRequest(String alarmName, Collection<String> actions, Collection<String> metricNames, Collection<Dimension> dimensions, String namespace) {
		PutMetricAlarmRequest request = new PutMetricAlarmRequest();
        request.setAlarmName(alarmName);
        
        for (String metricName : metricNames) {
        	request.setMetricName(metricName);
        }

        request.setDimensions(dimensions);
        request.setNamespace(namespace);
        request.setComparisonOperator(ComparisonOperator.GreaterThanThreshold);
        request.setStatistic(Statistic.Average);
        request.setUnit(StandardUnit.Percent);
        request.setThreshold(60d);
        request.setPeriod(300);
        request.setEvaluationPeriods(2);
        request.setAlarmActions(actions);

        cwClient.putMetricAlarm(request);
	}
}
