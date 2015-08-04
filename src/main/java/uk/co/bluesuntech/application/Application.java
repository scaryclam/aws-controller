package uk.co.bluesuntech.application;

import java.awt.List;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.Listener;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

import uk.co.bluesuntech.config.OptionFactory;
import uk.co.bluesuntech.delta.DeltaApplier;
import uk.co.bluesuntech.ec2.EC2Client;
import uk.co.bluesuntech.ec2.EC2EnvironmentCreator;
import uk.co.bluesuntech.elasticloadbalancer.ELBEnvironmentCreator;
import uk.co.bluesuntech.elasticloadbalancer.LoadBalancerClient;
import uk.co.bluesuntech.export.Exporter;
import uk.co.bluesuntech.importer.Importer;
import uk.co.bluesuntech.rds.RDSClient;
import uk.co.bluesuntech.rds.RDSEnvironmentCreator;


public class Application {
	private static String AWS_ACCESS_KEY_ID;
	private static String AWS_SECRET_ACCESS_KEY_ID;
	private CommandLine setup;
	private static Options options;
	
	public Application (CommandLine line) {
		setup = line;
	}
	
	private void executeCreation(String environment) throws JSONException, IOException {
	    // Creation mode
	    Exporter exporter = new Exporter();
        JSONObject currentConfig = exporter.exportExploration();
        boolean hasOutput = setup.hasOption('o');
        boolean hasInput = setup.hasOption('i');
        
        if (!hasInput) {
            System.out.println("ERROR: you must provide an input file");
        }
        
        Importer importer = new Importer();
        String inputFile = setup.getOptionValue('i');
        System.out.println("Input file set, attempting to load configuration");
        JSONObject newFullConfig = importer.readConfigFromFile(inputFile);
        JSONObject fullConfig = newFullConfig.getJSONObject(environment); 
        
        // Create the things!
        // EC2
        EC2EnvironmentCreator ec2Creator = new EC2EnvironmentCreator();
        ec2Creator.createEnvFromConfig(fullConfig.getJSONObject("ec2"));
        
        // ELB
        ELBEnvironmentCreator elbCreator = new ELBEnvironmentCreator();
        elbCreator.createEnvFromConfig(fullConfig.getJSONObject("elb"));
        
        // RDS
        RDSEnvironmentCreator rdsCreator = new RDSEnvironmentCreator();
        rdsCreator.createEnvFromConfig(fullConfig.getJSONObject("rds"));
        
        
        // Write the new configuration out
        if (hasOutput) {
            System.out.println("Output option set. Attempting to write configuration");
            String outputFile = setup.getOptionValue('o');
            // Re-scan the infrastructure
            currentConfig = exporter.exportExploration();
            exporter.writeConfig(outputFile, fullConfig, currentConfig, environment);
        }
	}
	
	private void executeTearDown() {
        // Tear down mode
    }
	
	private void executeDelta() {
        // Delta mode
    }
	
	private void executeExplore(String environment) throws JSONException, IOException {
	    // Exploratory mode
	    Exporter exporter = new Exporter();
        JSONObject currentConfig = exporter.exportExploration();
        JSONObject newFullConfig = null;
        boolean hasOutput = setup.hasOption('o');
        boolean hasInput = setup.hasOption('i');
        
        System.out.println("Current configuration:");
        System.out.println(currentConfig.toString(4));
        
        if (hasInput) {
            Importer importer = new Importer();
            String inputFile = setup.getOptionValue('i');
            System.out.println("Input file set, attempting to load configuration");
            newFullConfig = importer.readConfigFromFile(inputFile);
        }
        
        if (hasOutput) {
            System.out.println("Output option set. Attempting to write configuration");
            String outputFile = setup.getOptionValue('o');
            if (newFullConfig == null) {
                newFullConfig = exporter.createEmptyConfig();
            }
            exporter.writeConfig(outputFile, newFullConfig, currentConfig, environment);
        }
	}
	
	private void execute() throws IOException, JSONException {
	    /* TODO: change the application to allow the following modes:
	     *  - Creating an environment
	     *  - Tearing down an environment
	     *  - Applying a delta of an existing environment
	     */
	    String environment = setup.getOptionValue('e', "default");
		boolean help = setup.hasOption('h');
		//boolean noInput = setup.hasOption('y');
		//boolean showDiff = setup.hasOption('d');
		//boolean dryRun = setup.hasOption("dry-run") || setup.hasOption('D');
		
		//boolean hasInput = setup.hasOption('i');
		//boolean hasOutput = setup.hasOption('o');
		
		String mode = setup.getOptionValue("mode");
		
		if (help) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar aws-controller.jar", options);
            System.exit(0);
        }
		
		// We always need to export the current config somewhere, whether it's to screen or
        // to file, so go get the current landscape.
        //Exporter exporter = new Exporter();
        //JSONObject currentConfig = exporter.exportExploration();
		
		switch(mode) {
		   case "create":
		       try {
		           executeCreation(environment);
		           break;
		       } catch (Exception error) {
		           HelpFormatter formatter = new HelpFormatter();
		           formatter.printHelp("java -jar aws-controller.jar", options);
		           System.exit(0);
		       }
		   case "teardown":
		       executeTearDown();
               break;
		   case "delta":
		       executeDelta();
               break;
		   case "explore":
		       executeExplore(environment);
		       break;
		   default:
		       HelpFormatter formatter = new HelpFormatter();
	           formatter.printHelp("java -jar aws-controller.jar", options);
	           System.exit(0);
		}
		
//		JSONObject delta = new JSONObject();
//		JSONObject newFullConfig = null;
//		
//		if (hasInput) {
//		    Importer importer = new Importer();
//			String inputFile = setup.getOptionValue('i');
//			System.out.println("Input file set, attempting to load configuration");
//			newFullConfig = importer.readConfigFromFile(inputFile);
//			JSONObject newConfig = importer.getEnvironmentConfig(newFullConfig, environment);
//			delta = importer.createDelta(newConfig, currentConfig);
//		}
//		
//		JSONObject changedConfig = currentConfig;
//		
//		if (showDiff) {
//			// Tell user what changes will be made
//			System.out.println("Changes to be made:");
//			System.out.println(delta.toString(4));
//		}
//		
//		if (hasOutput) {
//			System.out.println("Output option set. Attempting to write configuration");
//			String outputFile = setup.getOptionValue('o');
//			if (newFullConfig == null) {
//			    newFullConfig = exporter.createEmptyConfig();
//			}
//			exporter.writeConfig(outputFile, newFullConfig, changedConfig, environment);
//		}
//		
//		if (!dryRun) {
//			Boolean applyChanges = false;
//			if (noInput) {
//				applyChanges = true;
//			} else {
//				System.out.println("Would you like to apply the changes? [N/y]");
//				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//			    String input = reader.readLine();
//				if (input.toLowerCase().equals("y")) {
//					applyChanges = true;
//				}
//			}
//			if (applyChanges) {
//			    // Apply Changes
//			    DeltaApplier applier = new DeltaApplier();
//			    applier.applyDelta(delta);
//			} else {
//				System.out.println("No changes have been applied");
//			}
//		}

		//LoadBalancerClient lbClient = new LoadBalancerClient();
		
		//Collection<String> availabilityZones = new ArrayList<String>();
		//availabilityZones.add("eu-west-1a");
		//availabilityZones.add("eu-west-1b");
		//availabilityZones.add("eu-west-1c");
		
		//Collection<Listener> listeners = new ArrayList<Listener>();
		//Listener listener = lbClient.createListener("http", 80, 80);
		//listeners.add(listener);
		//lbClient.createLoadBalancer("test-balancer-2", groups, listeners, availabilityZones);
		
		System.out.println("Finished");
	}
	
	public static void main(String[] args) {
		CommandLineParser parser = new PosixParser();
		options = OptionFactory.buildOptions();
		
		try {
			CommandLine line = parser.parse(options, args);
			Application app = new Application(line);
			app.execute();
		} catch (Exception e) {
			e.printStackTrace();
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar aws-controller.jar", options);
		}
	}
}
