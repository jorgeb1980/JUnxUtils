package unxutils.coreutils;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

// Test utilities for the command line applications
public class TestUtils {

	// Builds a command line from an options set
	public static CommandLine getLine(Options options, String line) {
		CommandLine commandLine = null;
		try {
			commandLine = new DefaultParser().parse(options, line.split(""));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return commandLine;
	}
	
	public static void deleteDir(File file) {
	    File[] contents = file.listFiles();
	    if (contents != null) {
	        for (File f : contents) {
	            deleteDir(f);
	        }
	    }
	    file.delete();
	}


}
