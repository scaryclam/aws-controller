package uk.co.bluesuntech.elasticloadbalancer;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.elasticloadbalancing.model.Listener;
import com.amazonaws.services.elasticloadbalancing.model.Tag;
import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;


public class ELBEnvironmentCreator {
    LoadBalancerClient elbClient;
    
    public ELBEnvironmentCreator() {
        elbClient = new LoadBalancerClient();
    }
    
    public ELBEnvironmentCreator(LoadBalancerClient inputELBClient) {
        elbClient = inputELBClient;
    }
    
    public void createEnvFromConfig(JSONObject config) throws JSONException {
        createLoadBalancers(config);
    }
    
    private void createLoadBalancers(JSONObject config) throws JSONException {
        JSONArray loadBalancers = config.getJSONArray("loadBalancers");
        for (int index = 0; index < loadBalancers.length(); index++) {
            JSONObject loadBalancer = loadBalancers.getJSONObject(index);
            
            String loadBalancerName = loadBalancer.getString("name");
            List<String> securityGroups = new ArrayList<String>();
            JSONArray groups = loadBalancer.getJSONArray("securityGroups");
            for (int i = 0; i < groups.length(); i++) {
                securityGroups.add(groups.getString(i));
            }
            
            List<Listener> listeners = new ArrayList<Listener>();
            JSONArray groupListeners = loadBalancer.getJSONArray("listenerDescriptions");
            for (int i = 0; i < groupListeners.length(); i++) {
                JSONObject listenerDesc = groupListeners.getJSONObject(i).getJSONObject("listener");
                Listener listener = elbClient.createListener(listenerDesc.getString("protocol"),
                                                             listenerDesc.getInt("loadBalancerPort"),
                                                             listenerDesc.getInt("instancePort"));
                listeners.add(listener);
            }
            
            List<String> availabilityZones = new ArrayList<String>();
            JSONArray azs = loadBalancer.getJSONArray("availabilityZones");
            for (int i = 0; i < azs.length(); i++) {
                availabilityZones.add(azs.getString(i));
            }
            
            List<Tag> tags = new ArrayList<Tag>();
//            JSONArray lbTags = loadBalancer.getJSONArray("tags");
//            for (int i = 0; i < lbTags.length(); i++) {
//                JSONObject tag = lbTags.getJSONObject(i);
//                Tag newTag = new Tag();
//                tag.setKey(tag);
//                tags.add(newTag);
//            }
            elbClient.createLoadBalancer(loadBalancerName, securityGroups, listeners, availabilityZones, tags);
        }
    }
}
