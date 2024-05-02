package unxutils.coreutils;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static unxutils.coreutils.TestUtils.deleteDir;
import static unxutils.coreutils.TestUtils.runCommand;

/**
 * This class tests the ls command
 */
public class TestListDirectoryCommand {

	private static final int DOT_FILES = 3;
	private static final List<File> dotFiles = new LinkedList<>();
	private static final int BACKUP_FILES = 4;
	private static final List<File> backupFiles = new LinkedList<>();
	private static final int ABC_FILES = 3;
	private static final List<File> abcFiles = new LinkedList<>();

	private static File directory = null;

	@AfterAll
	public static void cleanup() {
		deleteDir(directory);
	}

	@BeforeAll
	public static void populateDirectory() {
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
			fail(ioe);
		}
	}
	
	@Test
	public void testSimpleFiles() {
		var command = new ListDirectoryCommand();
		command.setIgnoreBackups(true);
		try {
			var ctx = runCommand(() -> command.execute(directory.toPath()));
			System.out.print("Output:\n" + ctx.getOut());
			var lines = new LinkedList<>(ctx.getOut().lines().toList());
			Collections.sort(lines);
			// No files starting with dot, no backups (ending with ~)
			var sortedFiles = abcFiles.stream().map(File::getName).sorted().toList();
			assertEquals(sortedFiles, lines);
			assertEquals("", ctx.getErr());
		} catch(Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void listSingleFile() {
		var command = new ListDirectoryCommand();
		command.setFiles(Arrays.asList(dotFiles.getFirst().getName()));
		try {
			var ctx = runCommand(() -> command.execute(directory.toPath()));
			System.out.print("Output:\n" + ctx.getOut());
			assertEquals(dotFiles.getFirst().getName().trim(), ctx.getOut().trim());
		} catch(Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void listAllFiles() {
		var command = new ListDirectoryCommand();
		command.setAll(true);
		try {
			var ctx = runCommand(() -> command.execute(directory.toPath()));
			var lines = new LinkedList<>(ctx.getOut().lines().toList());
			dotFiles.forEach(file -> { assertTrue(lines.contains(file.getName())); });
			backupFiles.forEach(file -> { assertTrue(lines.contains(file.getName())); });
			abcFiles.forEach(file -> { assertTrue(lines.contains(file.getName())); });
		} catch(Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void listAbcFiles() {
		var command = new ListDirectoryCommand();
		command.setAll(true);
		// Ignore those not containing 'abc'
		command.setIgnore("^((?!abc).)*$");
		try {
			var ctx = runCommand(() -> command.execute(directory.toPath()));
			var lines = new LinkedList<>(ctx.getOut().lines().toList());
			dotFiles.forEach(file -> { assertFalse(lines.contains(file.getName())); });
			backupFiles.forEach(file -> { assertFalse(lines.contains(file.getName())); });
			abcFiles.forEach(file -> { assertTrue(lines.contains(file.getName())); });
		} catch(Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void listNonAbcFiles() {
		var command = new ListDirectoryCommand();
		command.setAll(true);
		// Ignore those containing 'abc'
		command.setIgnore("(abc)");
		try {
			var ctx = runCommand(() -> command.execute(directory.toPath()));
			var lines = new LinkedList<>(ctx.getOut().lines().toList());
			dotFiles.forEach(file -> { assertTrue(lines.contains(file.getName())); });
			backupFiles.forEach(file -> { assertTrue(lines.contains(file.getName())); });
			abcFiles.forEach(file -> { assertFalse(lines.contains(file.getName())); });
		} catch(Exception e) {
			fail(e.getMessage());
		}
	}
}
