package unxutils.coreutils;

import java.nio.file.Path;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import unxutils.common.Command;

/**
<b>Program documentation</b><br>
<pre>
{@code
The ls program lists information about files (of any type, including directories). 
Options and file arguments can be intermixed arbitrarily, as usual.

For non-option command-line arguments that are directories, by default ls lists 
the contents of directories, not recursively, and omitting files with names 
beginning with ‘.’. For other non-option arguments, by default ls lists just the 
file name. If no non-option argument is specified, ls operates on the current 
directory, acting as if it had been invoked with a single argument of ‘.’.

By default, the output is sorted alphabetically, according to the locale settings 
in effect.3 If standard output is a terminal, the output is in columns (sorted 
vertically) and control characters are output as question marks; otherwise, the 
output is listed one per line and control characters are output as-is.

Because ls is such a fundamental program, it has accumulated many options over 
the years. They are described in the subsections below; within each section, 
options are listed alphabetically (ignoring case). The division of options into 
the subsections is not absolute, since some options affect more than one aspect 
of ls’s operation.

Exit status:

0 success
1 minor problems  (e.g., failure to access a file or directory not
  specified as a command line argument.  This happens when listing a
  directory in which entries are actively being removed or renamed.)
2 serious trouble (e.g., memory exhausted, invalid option, failure
  to access a file or directory specified as a command line argument
  or a directory loop)
}
</pre>
<br>
<b>Parameters</b><br>
<pre>
{@code
These options determine which files ls lists information for. By default, ls 
lists files and the contents of any directories on the command line, except that 
in directories it ignores files whose names start with ‘.’.

‘-a’
‘--all’
In directories, do not ignore file names that start with ‘.’.

‘-A’
‘--almost-all’
In directories, do not ignore all file names that start with ‘.’; ignore only . 
and ... The --all (-a) option overrides this option.

‘-B’
‘--ignore-backups’
In directories, ignore files that end with ‘~’. This option is equivalent to 
‘--ignore='*~' --ignore='.*~'’.

‘-d’
‘--directory’
List just the names of directories, as with other types of files, rather than 
listing their contents. Do not follow symbolic links listed on the command line 
unless the --dereference-command-line (-H), --dereference (-L), or 
--dereference-command-line-symlink-to-dir options are specified.

‘-H’
‘--dereference-command-line’
If a command line argument specifies a symbolic link, show information for the 
file the link references rather than for the link itself.

‘--dereference-command-line-symlink-to-dir’
Do not dereference symbolic links, with one exception: if a command line argument 
specifies a symbolic link that refers to a directory, show information for that 
directory rather than for the link itself. This is the default behavior when no 
other dereferencing-related option has been specified (--classify (-F), 
--directory (-d), (-l), --dereference (-L), or --dereference-command-line (-H)).

‘--group-directories-first’
Group all the directories before the files and then sort the directories and the 
files separately using the selected sort key (see –sort option). That is, this 
option specifies a primary sort key, and the –sort option specifies a secondary 
key. However, any use of --sort=none (-U) disables this option altogether.

‘--hide=PATTERN’
In directories, ignore files whose names match the shell pattern pattern, unless 
the --all (-a) or --almost-all (-A) is also given. This option acts like 
--ignore=pattern except that it has no effect if --all (-a) or --almost-all (-A) 
is also given.

This option can be useful in shell aliases. For example, if lx is an alias for 
‘ls --hide='*~'’ and ly is an alias for ‘ls --ignore='*~'’, then the command 
‘lx -A’ lists the file README~ even though ‘ly -A’ would not.

‘-I pattern’
‘--ignore=pattern’
In directories, ignore files whose names match the shell pattern (not regular 
expression) pattern. As in the shell, an initial ‘.’ in a file name does not 
match a wildcard at the start of pattern. Sometimes it is useful to give this 
option several times. For example,

$ ls --ignore='.??*' --ignore='.[^.]' --ignore='#*'
The first option ignores names of length 3 or more that start with ‘.’, the 
second ignores all two-character names that start with ‘.’ except ‘..’, and 
the third ignores names that start with ‘#’.

‘-L’
‘--dereference’
When showing file information for a symbolic link, show information for the 
file the link references rather than the link itself. However, even with this 
option, ls still prints the name of the link itself, not the name of the file 
that the link points to.

‘-R’
‘--recursive’
List the contents of all directories recursively.

 --color[=WHEN]       colors the output. WHEN may be 'never', 'auto',
                               or 'always' (by default)
}
</pre>
*/
@Command(command="ls")
public class ListDirectoryCommand {
	
	//-----------------------------------------------------------------
	// Command properties
	
	/** Options defined for the command. */
	private Options options;
	/** Command line passed to the command. */
	private CommandLine commandLine;
	
	//-----------------------------------------------------------------
	// Command methods	

	// Entry point for ls
	public int execute(Path currentPath, List<String> args) {
		buildOptions();
		int ret = 0;
		try {
			parseCommandLine(args);
			printCommandLine();
		}
		catch(ParseException e) {
			ret = 2;
		}
		return ret;
	}
	
	private void printCommandLine() {
		for (Option opt: commandLine.getOptions()) {
			System.out.println(opt);
		}		
	}
	
	// Builds the ls options
	private void buildOptions() {
		options = new Options();
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
		options.addOption("I", "ignore", true, "do not list implied entries matching shell PATTERN");
		options.addOption("h", "human-readable", false, "with -l and/or -s, "
				+ "print human readable sizes (e.g., 1K 234M 2G)");
		options.addOption("L", "dereference", false, "list entries pointed to by symbolic links");
		options.addOption("R", "recursive", false, "list subdirectories recursively");
		options.addOption("l", false, "uses a long output format");
	}
	
	// Reads the command line with the group of options passed
	private void parseCommandLine(List<String> args) throws ParseException {
		commandLine =  new DefaultParser().parse(
				options, 
				args.toArray(new String[args.size()]),
				// Fail if there is something unrecognized
				false);
	}
}
