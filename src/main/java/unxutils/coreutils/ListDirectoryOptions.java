package unxutils.coreutils;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * This class groups all the options for the ls command
 */
class ListDirectoryOptions {
	
	//---------------------------------------------------------------
	// Class methods
	
	// Not instantiable
	private ListDirectoryOptions() {}
	
	/** 
	 * Builds the options set for the ls command.
	 * @return Options for the ls command
	 */
	public static Options build() {		
		Options options = new Options();
		options.addOption("a", "all", false, 
				"In directories, do not ignore file names that start with ‘.’");
		options.addOption("A", "almost-all", false, 
				"In directories, do not ignore all file names that start "
				+ "with ‘.’; ignore only . and ... The --all (-a) option overrides this option");
		options.addOption("B", "ignore-backups", false, 
				"In directories, ignore files that end with ‘~’. This option "
				+ "is equivalent to ‘--ignore='*~' --ignore='.*~'’");
		options.addOption("d", "directory", false, 
				"List just the names of directories, as with other types of files, "
				+ "rather than listing their contents. Do not follow symbolic links "
				+ "listed on the command line unless the --dereference-command-line (-H), "
				+ "dereference (-L), or --dereference-command-line-symlink-to-dir "
				+ "options are specified");	
		options.addOption("H", "dereference-command-line", false, 
				"If a command line argument specifies a symbolic link, show "
				+ "information for the file the link references rather than for the link itself");
		options.addOption(Option.builder().hasArg(false).longOpt("dereference-command-line-symlink-to-dir").
			desc("Do not dereference symbolic links, with one exception: if a "
					+ "command line argument specifies a symbolic link that refers "
					+ "to a directory, show information for that directory rather "
					+ "than for the link itself. This is the default behavior when "
					+ "no other dereferencing-related option has been specified "
					+ "(--classify (-F), --directory (-d), (-l), --dereference (-L), "
					+ "or --dereference-command-line (-H))").build());
		options.addOption(Option.builder().hasArg(false).longOpt("group-directories-first").
			desc("Group all the directories before the files and then sort "
					+ "the directories and the files separately using the "
					+ "selected sort key (see –sort option). That is, this "
					+ "option specifies a primary sort key, and the –sort "
					+ "option specifies a secondary key. However, any use of "
					+ "sort=none (-U) disables this option altogether").build());
		options.addOption(Option.builder().hasArg(true).longOpt("hide").
			desc("In directories, ignore files whose names match the shell pattern pattern,"
					+ " unless the --all (-a) or --almost-all (-A) is also given. This option "
					+ "acts like --ignore=pattern except that it has no effect if --all (-a) "
					+ "or --almost-all (-A) is also given").build());
		options.addOption(Option.builder().hasArg(false).longOpt("color").
			desc("colors the output. WHEN may be 'never', 'auto', or 'always' (by default)").build());
		options.addOption("I", "ignore", true, "do not list implied entries matching shell PATTERN");
		options.addOption("h", "human-readable", false, "with -l and/or -s, "
				+ "print human readable sizes (e.g., 1K 234M 2G)");
		options.addOption("L", "dereference", false, "list entries pointed to by symbolic links");
		// Depth limit??
		options.addOption("R", "recursive", false, "list subdirectories recursively");
		options.addOption("l", false, "uses a long output format");
		return options;
	}
}
