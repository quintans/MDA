package pt.quintans.mda;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import pt.quintans.mda.core.WorkerStore;

public class StartUp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Options options = new Options();

		options.addRequiredOption("w", "workflow", true, "workflow path relative to the working directory");
		options.addOption("q", "quiet", false, "produces fewer logs");
		options.addRequiredOption("sn", "subnamespace", true, "subnamespace");

		CommandLineParser parser = new DefaultParser();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);
			boolean quiet = line.hasOption("q");

			String subnamespace = line.getOptionValue("sn");
			String workflowFile = line.getOptionValue("w");

			WorkerStore.get().doIt(workflowFile, subnamespace, quiet);
			System.out.println("Generation ended...");
		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
		} catch (Exception e) {
			ByteArrayOutputStream ba = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(ba));
			System.err.println(ba.toString());
		}

	}

}
