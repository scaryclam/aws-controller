package uk.co.bluesuntech.autoscaling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.CreateAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.CreateLaunchConfigurationRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsResult;
import com.amazonaws.services.autoscaling.model.DescribeLaunchConfigurationsResult;
import com.amazonaws.services.autoscaling.model.DescribePoliciesResult;
import com.amazonaws.services.autoscaling.model.InstanceMonitoring;
import com.amazonaws.services.autoscaling.model.LaunchConfiguration;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyRequest;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyResult;
import com.amazonaws.services.autoscaling.model.ScalingPolicy;
import com.amazonaws.services.autoscaling.model.TagDescription;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.ComparisonOperator;
import com.amazonaws.services.cloudwatch.model.DescribeAlarmsResult;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricAlarm;
import com.amazonaws.services.cloudwatch.model.PutMetricAlarmRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.amazonaws.services.cloudwatch.model.Statistic;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

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
	
	private List<MetricAlarm> getScaleAlarms() {
		DescribeAlarmsResult result = cwClient.describeAlarms();
		return result.getMetricAlarms();
	}
	
	private List<AutoScalingGroup> getAutoscaleGroups() {
		DescribeAutoScalingGroupsResult result = asClient.describeAutoScalingGroups();
		return result.getAutoScalingGroups();
	}
	
	private List<LaunchConfiguration> getLaunchConfigurations() {
		DescribeLaunchConfigurationsResult result = asClient.describeLaunchConfigurations();
		return result.getLaunchConfigurations();
	}
	
	private List<ScalingPolicy> getAutoscalingPolicies() {
		DescribePoliciesResult result = asClient.describePolicies();
		return result.getScalingPolicies();
	}
	
	// JSON Fetchers
	public List<JSONObject> getAutoscalingPoliciesAsJson() throws JSONException {
		List<JSONObject> policies = new ArrayList<JSONObject>();
		List<ScalingPolicy> autoscalePolicies = getAutoscalingPolicies();
		for (ScalingPolicy policy : autoscalePolicies) {
			JSONObject currentPolicy = new JSONObject();
			currentPolicy.put("adjustmentType", policy.getAdjustmentType());
			currentPolicy.put("alarms", policy.getAlarms());
			currentPolicy.put("autoscalingGroupName", policy.getAutoScalingGroupName());
			currentPolicy.put("coolDown", policy.getCooldown());
			currentPolicy.put("minAdjustmentStep", policy.getMinAdjustmentStep());
			currentPolicy.put("arn", policy.getPolicyARN());
			currentPolicy.put("name", policy.getPolicyName());
			currentPolicy.put("scalingAdjustment", policy.getScalingAdjustment());
			policies.add(currentPolicy);
		}
		return policies;
	}
	
	public List<JSONObject> getLaunchConfigurationsAsJson() throws JSONException {
		List<JSONObject> configs = new ArrayList<JSONObject>();
		List<LaunchConfiguration> launchConfigs = getLaunchConfigurations();
		for (LaunchConfiguration config : launchConfigs) {
			JSONObject currentConfig = new JSONObject();
			currentConfig.put("keyName", config.getKeyName());
			currentConfig.put("associatePublicIP", config.getAssociatePublicIpAddress());
			currentConfig.put("blockDeviceMappings", config.getBlockDeviceMappings());
			currentConfig.put("classicLinkVpcId", config.getClassicLinkVPCId());
			currentConfig.put("classicLinkVpcSecurityGroups", config.getClassicLinkVPCSecurityGroups());
			currentConfig.put("ebsOptimized", config.getEbsOptimized());
			currentConfig.put("iamInstanceProfile", config.getIamInstanceProfile());
			currentConfig.put("imageId", config.getImageId());
			currentConfig.put("instanceMonitoring", config.getInstanceMonitoring());
			currentConfig.put("instanceType", config.getInstanceType());
			currentConfig.put("kernelId", config.getKernelId());
			currentConfig.put("arn", config.getLaunchConfigurationARN());
			currentConfig.put("name", config.getLaunchConfigurationName());
			currentConfig.put("placemenTenancy", config.getPlacementTenancy());
			currentConfig.put("ramDiskId", config.getRamdiskId());
			currentConfig.put("securityGroups", config.getSecurityGroups());
			currentConfig.put("spotPrice", config.getSpotPrice());
			currentConfig.put("userData", config.getUserData());
			configs.add(currentConfig);
		}
		return configs;
	}
	public List<JSONObject> getGroupsAsJson() throws JSONException {
		List<JSONObject> groups = new ArrayList<JSONObject>();
		List<AutoScalingGroup> autoscaleGroups = getAutoscaleGroups();
		for (AutoScalingGroup group : autoscaleGroups) {
			JSONObject currentGroup = new JSONObject();
			currentGroup.put("name", group.getAutoScalingGroupName());
			currentGroup.put("arn", group.getAutoScalingGroupARN());
			currentGroup.put("availabilityZones", group.getAvailabilityZones());
			currentGroup.put("defaultCoolDown", group.getDefaultCooldown());
			currentGroup.put("desiredCapacity", group.getDesiredCapacity());
			currentGroup.put("enabledMetrics", group.getEnabledMetrics());
			currentGroup.put("healthCheckGracePeriod", group.getHealthCheckGracePeriod());
			currentGroup.put("healthCheckType", group.getHealthCheckType());
			currentGroup.put("launchConfigurationName", group.getLaunchConfigurationName());
			currentGroup.put("loadBalancerNames", group.getLoadBalancerNames());
			currentGroup.put("maxSize", group.getMaxSize());
			currentGroup.put("minSize", group.getMinSize());
			currentGroup.put("placementGroup", group.getPlacementGroup());
			currentGroup.put("status", group.getStatus());
			currentGroup.put("suspendedProcesses", group.getSuspendedProcesses());
			JSONObject tags = new JSONObject();
			for (TagDescription tag : group.getTags()) {
				tags.put(tag.getKey(), tag.getValue());
			}
			currentGroup.put("tags", tags);
			currentGroup.put("terminatedPolicies", group.getTerminationPolicies());
			currentGroup.put("vpczoneId", group.getVPCZoneIdentifier());
			groups.add(currentGroup);
		}
		return groups;
	}
	public List<JSONObject> getAlarmsAsJson() throws JSONException {
		List<JSONObject> alarms = new ArrayList<JSONObject>();
		List<MetricAlarm> metricAlarms = getScaleAlarms(); 
		
		for (MetricAlarm alarm : metricAlarms) {
			JSONObject currentAlarm = new JSONObject();
			currentAlarm.put("name", alarm.getAlarmName());
			currentAlarm.put("arn", alarm.getAlarmArn());
			currentAlarm.put("description", alarm.getAlarmDescription());
			currentAlarm.put("actionsEnabled", alarm.getActionsEnabled());
			currentAlarm.put("actions", alarm.getAlarmActions());
			currentAlarm.put("comparisonOperator", alarm.getComparisonOperator());
			currentAlarm.put("dimensions", alarm.getDimensions());
			currentAlarm.put("evaluationPeriods", alarm.getEvaluationPeriods());
			currentAlarm.put("insufficientDataActions", alarm.getInsufficientDataActions());
			currentAlarm.put("matricName", alarm.getMetricName());
			currentAlarm.put("namespace", alarm.getNamespace());
			currentAlarm.put("okActions", alarm.getOKActions());
			currentAlarm.put("period", alarm.getPeriod());
			currentAlarm.put("stateReason", alarm.getStateReason());
			currentAlarm.put("stateReasonData", alarm.getStateReasonData());
			currentAlarm.put("stateValue", alarm.getStateValue());
			currentAlarm.put("statistic", alarm.getStatistic());
			currentAlarm.put("threshold", alarm.getThreshold());
			currentAlarm.put("unit", alarm.getUnit());
			alarms.add(currentAlarm);
		}
		return alarms;
	}
}
