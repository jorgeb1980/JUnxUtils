package unxutils.coreutils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * This class tests the ls command
 */
public class TestListDirectoryCommand {

	private static final int DOT_FILES = 3;
	private static final int BACKUP_FILES = 4;
	private static final int ABC_FILES = 3;
	
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
				f.createNewFile();
			}
			// Files ending with ~
			for (int i = 0; i < BACKUP_FILES; i++) {
				File f = new File(directory, "backup" + i + "~");
				f.createNewFile();
			}
			// Files containing abc in some place of its name
			for (int i = 0; i < ABC_FILES; i++) {
				File f = new File(directory, RandomStringUtils.random(3) + "abc" + RandomStringUtils.random(4));
				f.createNewFile();
			}
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	@Test
	public void testAll() {
		
	}
}
