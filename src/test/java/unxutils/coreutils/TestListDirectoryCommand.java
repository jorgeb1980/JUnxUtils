package unxutils.coreutils;

import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static test.Sandbox.sandbox;

/**
 * This class tests the ls command
 */
public class TestListDirectoryCommand {

	private static final int DOT_FILES = 3;
	private static final int BACKUP_FILES = 4;
	private static final int ABC_FILES = 3;

	@Getter
	private static class DirectoryContext {
		private final List<File> dotFiles = new LinkedList<>();
		private final List<File> backupFiles = new LinkedList<>();
		private final List<File> abcFiles = new LinkedList<>();

		public void populateDirectory(File directory) throws IOException {
			// Populate the directory
			// Files starting with .
			for (int i = 0; i < DOT_FILES; i++) {
				var f = new File(directory, ".dotFile" + i);
				if (f.createNewFile()) dotFiles.add(f);
			}
			// Files ending with ~
			for (int i = 0; i < BACKUP_FILES; i++) {
				var f = new File(directory, "backup" + i + "~");
				if (f.createNewFile()) backupFiles.add(f);
			}
			// Files containing abc in some place of its name
			for (int i = 0; i < ABC_FILES; i++) {
				var f = new File(directory, randomAlphanumeric(3) + "abc" + randomAlphanumeric(4));
				if (f.createNewFile()) abcFiles.add(f);
			}
		}
	}


	
	@Test
	public void testSimpleFiles() {
		var command = new ListDirectoryCommand();
		command.setIgnoreBackups(true);
		var dirCtx = new DirectoryContext();
		var ctx = sandbox().runTest(
			(File directory) -> {
				dirCtx.populateDirectory(directory);
				return command.execute(directory.toPath());
			},
			true
		);
		System.out.print("Output:\n" + ctx.out());
		var lines = new LinkedList<>(ctx.out().lines().toList());
		Collections.sort(lines);
		// No files starting with dot, no backups (ending with ~)
		var sortedFiles = dirCtx.getAbcFiles().stream().map(File::getName).sorted().toList();
		assertEquals(sortedFiles, lines);
		assertEquals("", ctx.err());
	}

	@Test
	public void listSingleFile() {
		var command = new ListDirectoryCommand();
		var dirCtx = new DirectoryContext();
		var ctx = sandbox().runTest(
			(File directory) -> {
				dirCtx.populateDirectory(directory);
				command.setFiles(List.of(dirCtx.getDotFiles().getFirst().getName()));
				return command.execute(directory.toPath());
			},
			true
		);
		System.out.print("Output:\n" + ctx.out());
		assertEquals(dirCtx.getDotFiles().getFirst().getName().trim(), ctx.out().trim());
	}

	@Test
	public void listAllFiles() {
		var command = new ListDirectoryCommand();
		var dirCtx = new DirectoryContext();
		command.setAll(true);
		var ctx = sandbox().runTest(
			(File directory) -> {
				dirCtx.populateDirectory(directory);
				return command.execute(directory.toPath());
			},
		true
		);
		var lines = new LinkedList<>(ctx.out().lines().toList());
		dirCtx.getDotFiles().forEach(file -> assertTrue(lines.contains(file.getName())));
		dirCtx.getBackupFiles().forEach(file -> assertTrue(lines.contains(file.getName())));
		dirCtx.getAbcFiles().forEach(file -> assertTrue(lines.contains(file.getName())));
	}

	@Test
	public void listAbcFiles() {
		var command = new ListDirectoryCommand();
		var dirCtx = new DirectoryContext();
		command.setAll(true);
		// Ignore those not containing 'abc'
		command.setIgnore("^((?!abc).)*$");
		var ctx = sandbox().runTest(
			(File directory) -> {
				dirCtx.populateDirectory(directory);
				return command.execute(directory.toPath());
			},
			true
		);
		var lines = new LinkedList<>(ctx.out().lines().toList());
		dirCtx.getDotFiles().forEach(file -> assertFalse(lines.contains(file.getName())));
		dirCtx.getBackupFiles().forEach(file -> assertFalse(lines.contains(file.getName())));
		dirCtx.getAbcFiles().forEach(file -> assertTrue(lines.contains(file.getName())));
	}

	@Test
	public void listNonAbcFiles() {
		var command = new ListDirectoryCommand();
		var dirCtx = new DirectoryContext();
		command.setAll(true);
		// Ignore those containing 'abc'
		command.setIgnore("(abc)");
		var ctx = sandbox().runTest(
			(File directory) -> {
				dirCtx.populateDirectory(directory);
				return command.execute(directory.toPath());
			},
			true
		);
		var lines = new LinkedList<>(ctx.out().lines().toList());
		dirCtx.getDotFiles().forEach(file -> assertTrue(lines.contains(file.getName())));
		dirCtx.getBackupFiles().forEach(file -> assertTrue(lines.contains(file.getName())));
		dirCtx.getAbcFiles().forEach(file -> assertFalse(lines.contains(file.getName())));
	}
}
