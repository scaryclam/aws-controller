package uk.co.bluesuntech.ec2;

import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;


public class SecurityGroupClient {
	public void createSecurityGroup(String groupName) {
		CreateSecurityGroupRequest request = new CreateSecurityGroupRequest();
		request.setGroupName(groupName);
	}
}
