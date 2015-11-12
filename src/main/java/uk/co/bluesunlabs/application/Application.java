package uk.co.bluesunlabs.application;

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

import uk.co.bluesunlabs.config.OptionFactory;
import uk.co.bluesunlabs.delta.DeltaApplier;
import uk.co.bluesunlabs.ec2.EC2Client;
import uk.co.bluesunlabs.ec2.EC2EnvironmentCreator;
import uk.co.bluesunlabs.elasticloadbalancer.ELBEnvironmentCreator;
import uk.co.bluesunlabs.elasticloadbalancer.LoadBalancerClient;
import uk.co.bluesunlabs.export.Exporter;
import uk.co.bluesunlabs.importer.Importer;
import uk.co.bluesunlabs.rds.RDSClient;
import uk.co.bluesunlabs.rds.RDSEnvironmentCreator;


public class Application {
	private static String AWS_ACCESS_KEY_ID;
	private static String AWS_SECRET_ACCESS_KEY_ID;
	private CommandLine setup;
	private static Options options;
	
	public Application(CommandLine line) {
		setup = line;
	}
	
	private void execute() throws IOException, JSONException {
	    /* TODO: change the application to allow the following modes:
	     *  - Creating an environment
	     *  - Tearing down an environment
	     */
		ConfigReader configReader;
	    String environment = setup.getOptionValue('e', "default");
		boolean help = setup.hasOption('h');

		if (!setup.hasOption("mode")) {
			HelpFormatter formatter = new HelpFormatter();
			System.out.println("You must provide a mode!");
            formatter.printHelp("java -jar aws-controller.jar", options);
            System.exit(0);
		}
		String mode = setup.getOptionValue("mode");

		if (help) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar aws-controller.jar", options);
            System.exit(0);
        }
		
		String configPath = setup.getOptionValue("config");
		if (setup.hasOption("template")) {
			String templateName = setup.getOptionValue("template");
			configReader = new ConfigReader(configPath, templateName);
		} else {
			configReader = new ConfigReader(configPath);
		}
		
		JSONObject ec2Config = configReader.getEC2Config(configReader.getTemplate());
		EC2EnvironmentCreator ec2Creator = new EC2EnvironmentCreator();
		
		switch(mode) {
		   case "create":
		       try {
		    	   ec2Creator.createEnvFromConfig(ec2Config);
		           break;
		       } catch (Exception error) {
		           HelpFormatter formatter = new HelpFormatter();
		           formatter.printHelp("java -jar aws-controller.jar", options);
		           System.exit(0);
		       }
		   case "teardown":
		       ec2Creator.tearDownEnvFromConfig(ec2Config);
               break;
		   default:
		       HelpFormatter formatter = new HelpFormatter();
	           formatter.printHelp("java -jar aws-controller.jar", options);
	           System.exit(0);
		}		
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
