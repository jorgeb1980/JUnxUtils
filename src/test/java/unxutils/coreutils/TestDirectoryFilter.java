package unxutils.coreutils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.Files;

public class TestDirectoryFilter {
	
	@Test
	public void testAll() {
		File tmpDir = null;
		try {
			// Trivial test
			CommandLine commandLine = TestUtils.getLine(ListDirectoryOptions.build(), "");
			ListDirectoryFilter filter = new ListDirectoryFilter(commandLine);
			// Build a test set
			tmpDir = Files.createTempDir();
			List<File> allFiles = testSet1(tmpDir);
			
			List<File> filteredFiles = Arrays.asList(tmpDir.listFiles(filter));
			// Both lists should be the same
			Collections.sort(allFiles);
			Collections.sort(filteredFiles);
			Assert.assertArrayEquals(
				allFiles.toArray(new File[allFiles.size()]), 
				filteredFiles.toArray(new File[filteredFiles.size()]));
		}
		catch(Exception e) {
			Assert.fail();
		}
		finally {
			TestUtils.deleteDir(tmpDir);
		}
		
	}

	// Simple test set with 3 files and a directory
	private List<File> testSet1(File tmpDir) throws IOException {
		List<File> allFiles = new LinkedList<>();
		for (int i = 0; i < 3; i++) {
			File f = new File(tmpDir, "file" + i + ".txt");
			f.createNewFile();
			allFiles.add(f);
		}
		File dir = new File(tmpDir, "dir");
		dir.mkdirs();
		allFiles.add(dir);
		return allFiles;
	}
}
