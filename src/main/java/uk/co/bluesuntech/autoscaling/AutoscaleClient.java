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
	
	/* Setting up basic autoscaling is easy.
	 * 1) Make a launch configuration. This is the EC2 instance information.
	 * 2) Create your autoscaling group. This sets things like which AZ to run in and which health check type to use.
	 * 3) Set up the scaling policies. You should also setup the alarms to use with them in this step.
	 */
	
	public AutoscaleClient() {
		asClient = new AmazonAutoScalingClient();
		Region region = Region.getRegion(Regions.EU_WEST_1);
		asClient.setRegion(region);
		
		cwClient = new AmazonCloudWatchClient();
		cwClient.setRegion(region);
	}
	
	public void createLaunchConfiguration(String launchConfigName, String imageId, String instanceType, Collection<String> securityGroups, String keyName, InstanceMonitoring instanceMonitoring) {
		CreateLaunchConfigurationRequest request = new CreateLaunchConfigurationRequest();
        request.setLaunchConfigurationName(launchConfigName);
        request.setImageId(imageId);
        request.setInstanceType(instanceType);
        request.setSecurityGroups(securityGroups);
        request.setKeyName(keyName);
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
	
	private void createScaleAlarmRequest(String alarmName, Collection<String> actions, Collection<String> metricNames, Collection<Dimension> dimensions, String namespace, ComparisonOperator comparisonOperator, Integer period, Integer evalPeriods, Double threshold) {
		PutMetricAlarmRequest request = new PutMetricAlarmRequest();
        request.setAlarmName(alarmName);
        
        for (String metricName : metricNames) {
        	request.setMetricName(metricName);
        }

        request.setDimensions(dimensions);
        request.setNamespace(namespace);
        request.setComparisonOperator(comparisonOperator);
        request.setStatistic(Statistic.Average);
        request.setUnit(StandardUnit.Percent);
        request.setThreshold(threshold);
        request.setPeriod(period);
        request.setEvaluationPeriods(evalPeriods);
        request.setAlarmActions(actions);

        cwClient.putMetricAlarm(request);
	}
	
	public void createScaleUpAlarm(String alarmName, Collection<String> actions, Collection<String> metricNames, Collection<Dimension> dimensions, String nameSpace, Integer period, Integer evalPeriods, Double threshold) {
        createScaleAlarmRequest(alarmName, actions, metricNames, dimensions, nameSpace, ComparisonOperator.GreaterThanThreshold, period, evalPeriods, threshold);
	}
	
	public void createScaleDownAlarm(String alarmName, Collection<String> actions, Collection<String> metricNames, Collection<Dimension> dimensions, String nameSpace, Integer period, Integer evalPeriods, Double threshold) {
		createScaleAlarmRequest(alarmName, actions, metricNames, dimensions, nameSpace, ComparisonOperator.LessThanThreshold, period, evalPeriods, threshold);
	}
}
