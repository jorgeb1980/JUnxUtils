package unxutils.coreutils;

import cli.ExecutionContext;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This class tests the ls command
 */
public class TestListDirectoryCommand {

	private static final int DOT_FILES = 3;
	private static List<File> dotFiles = new LinkedList<>();
	private static final int BACKUP_FILES = 4;
	private static List<File> backupFiles = new LinkedList<>();
	private static final int ABC_FILES = 3;
	private static List<File> abcFiles = new LinkedList<>();
	
	private File directory = null;
	
	@Before
	public void populateDirectory() {
		try {
			directory = Files.createTempDirectory("tmp").toFile();
			directory.deleteOnExit();
			
			// Populate the directory
			// Files starting with .
			for (int i = 0; i < DOT_FILES; i++) {
				File f = new File(directory, ".dotFile" + i);
				if (f.createNewFile()) dotFiles.add(f);
			}
			// Files ending with ~
			for (int i = 0; i < BACKUP_FILES; i++) {
				File f = new File(directory, "backup" + i + "~");
				if (f.createNewFile()) backupFiles.add(f);
			}
			// Files containing abc in some place of its name
			for (int i = 0; i < ABC_FILES; i++) {
				File f = new File(directory, RandomStringUtils.random(3) + "abc" + RandomStringUtils.random(4));
				if (f.createNewFile()) abcFiles.add(f);
			}
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private static class TestExecutionContext {
		private ExecutionContext ctx;
		private PrintWriter stdOutput;
		private ByteArrayOutputStream stdBytes;
		private PrintWriter errOutput;
		private ByteArrayOutputStream errBytes;

		TestExecutionContext(File directory) {
			stdBytes = new ByteArrayOutputStream();
			stdOutput = new PrintWriter(new OutputStreamWriter(stdBytes, UTF_8));
			errBytes = new ByteArrayOutputStream();
			errOutput = new PrintWriter(new OutputStreamWriter(errBytes, UTF_8));
			ctx = new ExecutionContext(
				directory.toPath(),
				stdOutput,
				errOutput
			);
		}

		public ExecutionContext getCtx() { return ctx; }
		private String print(PrintWriter pw, ByteArrayOutputStream baos) {
			pw.flush();
			return baos.toString(UTF_8);
		}
		public String getStdOutput() { return print(stdOutput, stdBytes); }
		public String getErrOutput() { return print(errOutput, errBytes); }
	}
	
	@Test
	public void testSimpleFiles() {
		var ctx = new TestExecutionContext(directory);
		var command = new ListDirectoryCommand();
		try {
			command.execute(ctx.getCtx());
			System.out.print("Output:\n" + ctx.getStdOutput());
			var lines = new LinkedList<>(ctx.getStdOutput().lines().toList());
			Collections.sort(lines);
			// No files starting with dot, no backups (ending with ~)
			var sortedFiles = abcFiles.stream().map(f -> f.getName()).sorted().toList();
			Assert.assertEquals(sortedFiles.toArray(), lines.toArray());
			Assert.assertEquals("", ctx.getErrOutput());
		} catch(Exception e) {
			Assert.fail(e.getMessage());
		}
	}
}
