package uk.co.bluesunlabs.config;

import org.apache.commons.cli.Options;


public class OptionFactory {
	public static Options buildOptions() {
		Options options = new Options();
		options.addOption("h", "help", false, "Tool to help you");
		options.addOption("m", "mode", true, "The mode to use. Options are \"create\", \"teardown\", \"explore\" and \"delta\"");
		options.addOption("c", "config", true, "Use the specified config file");
		options.addOption("t", "template", true, "Name the template to use"); 
		
		return options;
	}
}
