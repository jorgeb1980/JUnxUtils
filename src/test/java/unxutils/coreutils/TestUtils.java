package unxutils.coreutils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.function.IntFunction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
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

	@Getter
	@Builder
	static class ExecutionContext {
		private String out;
		private String err;
		private Integer result;
	}

	static ExecutionContext runCommand(Callable<Integer> action) {
		final var myOut = new ByteArrayOutputStream();
		final var myErr = new ByteArrayOutputStream();
		final PrintStream originalOut = System.out;
		final PrintStream originalErr = System.err;
		System.setOut(new PrintStream(myOut));
		System.setErr(new PrintStream(myErr));
		String standardOutput = "";
		String standardErr = "";
		Integer result = null;
		try {
			result = action.call();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			standardOutput = myOut.toString();
			standardErr = myErr.toString();
			System.setOut(originalOut);
			System.setErr(originalErr);
		}
		return ExecutionContext.builder().out(standardOutput).err(standardErr).result(result).build();
	}
}
