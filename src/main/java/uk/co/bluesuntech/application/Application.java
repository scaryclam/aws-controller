package uk.co.bluesuntech.application;

import java.awt.List;
import java.io.IOException;
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
import uk.co.bluesuntech.ec2.EC2Client;
import uk.co.bluesuntech.elasticloadbalancer.LoadBalancerClient;
import uk.co.bluesuntech.export.Exporter;
import uk.co.bluesuntech.importer.Importer;
import uk.co.bluesuntech.rds.RDSClient;


public class Application {
	private static String AWS_ACCESS_KEY_ID;
	private static String AWS_SECRET_ACCESS_KEY_ID;
	private CommandLine setup;
	private static Options options;
	
	public Application (CommandLine line) {
		setup = line;
	}
	
	private void execute() throws IOException, JSONException {
		boolean help = setup.hasOption('h');
		boolean hasInput = setup.hasOption('i');
		boolean hasOutput = setup.hasOption('o');
		boolean noInput = setup.hasOption('y');
		
		if (help) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar aws-controller.jar", options);
			System.exit(0);
		}
		
		// We always need to export the current config somewhere, whether it's to screen or
		// to file, so go get the current landscape.
		Exporter exporter = new Exporter();
		JSONObject currentConfig = exporter.exportExploration();
		
		System.out.println("Current configuration:");
		System.out.println(currentConfig.toString(4));
		
		if (hasInput) {
			Importer importer = new Importer();
			String inputFile = setup.getOptionValue('i');
			System.out.println("Input file set, attempting to load configuration");
			JSONObject newConfig = importer.readConfigFromFile(inputFile);
			importer.createDelta(newConfig, currentConfig);
		}
		
		JSONObject changedConfig = currentConfig;
		
		if (hasOutput) {
			System.out.println("Output option set. Attempting to write configuration");
			String outputFile = setup.getOptionValue('o');
			exporter.writeConfig(outputFile, changedConfig);
		}

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
