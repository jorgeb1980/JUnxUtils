package unxutils.coreutils;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.cli.CommandLine;

/**
 * Implements a file filter based on the command line options. 
 * See <class>ListDirectoryCommand</class> documentation for the options.
 */
class ListDirectoryFilter implements FileFilter {

	//---------------------------------------------------------
	// Filter properties
	
	// Command line options
	private CommandLine options;	
	
	//---------------------------------------------------------
	// Filter methods
	
	/**
	 * Builds a filter based on the command line options.
	 * @param options Options passed to the filter.
	 */
	public ListDirectoryFilter(CommandLine options) {
		this.options = options;
	}
	
	@Override
	public boolean accept(File pathname) {
		boolean ret = true;
		
		return ret;
	}
}
