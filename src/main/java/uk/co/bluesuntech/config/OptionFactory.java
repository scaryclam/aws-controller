package uk.co.bluesuntech.config;

import org.apache.commons.cli.Options;


public class OptionFactory {
	public static Options buildOptions() {
		Options options = new Options();
		options.addOption("h", "help", false, "Tool to help you");
		options.addOption("o", "output", true, "Write output to file");
		options.addOption("i", "input", true, "Use specified input file");
		options.addOption("d", "diff", false,
				"Show a diff between in the input file and the current setup");
		options.addOption("D", "dry-run", false, "Perform all tasks normally, but do not apply changes");
		options.addOption("y", "no-input", false,
				"Do not ask for confirmation");
		options.addOption("e", "env", true, "Use an environment that is not \"default\"");
		options.addOption("m", "mode", true, "The mode to use. Options are \"create\", \"teardown\" and \"delta\"");
		return options;
	}
}
