package unxutils.coreutils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import unxutils.common.Command;
import unxutils.common.Parameter;
import unxutils.common.UnxException;

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

‘-R’
‘--recursive’
List the contents of all directories recursively.

 --color       colors the output
}
</pre>
*/
@Command(command="ls")
public class ListDirectoryCommand {
	
	//-----------------------------------------------------------------
	// Command parameters
	
	@Parameter(name="a", 
			longName="all", 
			description="In directories, do not ignore "
					+ "file names that start with ‘.’")
	private Boolean all = Boolean.FALSE;
	
	@Parameter(name="A", 
			longName="almost-all", 
			description="In directories, do not ignore all file names that start "
					+ "with ‘.’; ignore only . and .. The --all (-a) "
					+ "option overrides this option.")
	private Boolean almostAll = Boolean.FALSE;
	
	@Parameter(name="B", 
			longName="ignore-backups", 
			description="In directories, ignore files that end with ‘~’. This option "
				+ "is equivalent to ‘--ignore='*~' --ignore='.*~'’")
	private Boolean ignoreBackups = Boolean.TRUE;
	
	@Parameter(longName="group-directories-first", 
			description="Group all the directories before the files and then sort "
				+ "the directories and the files separately using the "
				+ "selected sort key (see –sort option). That is, this "
				+ "option specifies a primary sort key, and the –sort "
				+ "option specifies a secondary key. However, any use of "
				+ "sort=none (-U) disables this option altogether")
	private Boolean groupDirectoriesFirst = Boolean.FALSE;
	
	@Parameter(longName="hide", 
			hasArg=true,
			description="In directories, ignore files whose names match the shell pattern pattern,"
				+ " unless the --all (-a) or --almost-all (-A) is also given. This option "
				+ "acts like --ignore=pattern except that it has no effect if --all (-a) "
				+ "or --almost-all (-A) is also given")
	private String hide = null;
	
	@Parameter(longName="color", 
			description="colors the output.")
	private Boolean color = Boolean.FALSE;
	
	@Parameter(name="I",
			longName="ignore", 
			hasArg=true,
			description="do not list implied entries matching shell PATTERN")
	private String ignore = null;
	
	@Parameter(name="h",
			longName="human-readable", 
			description="print human readable sizes (e.g., 1K 234M 2G)")
	private Boolean humanReadable = Boolean.FALSE;
	
	@Parameter(name="R",
			longName="recursive", 
			description="list subdirectories recursively")
	private Boolean recursive = Boolean.FALSE;
	
	@Parameter(name="l",
			description="use a long listing format")
	private Boolean longOutputFormat = Boolean.FALSE;

	//-----------------------------------------------------------------
	// Command methods	
	
	/**
	 * Builds an ls command.
	 */
	public ListDirectoryCommand() {	}

	// Entry point for ls
	public int execute(
			final Path currentPath, 
			PrintWriter standardOutput, 
			PrintWriter errorOutput) throws UnxException {		
		int ret = 0;
		
		try {
			// Gather results, combining the appropriated filter options
			List<FileResult> results = listFiles(currentPath);
			// Render the result presentation, combining the appropriated
			//	output options
			for (FileResult f: results) {
				printFileResult(standardOutput, currentPath, f);
			}
			
			return ret;
		}
		catch(Exception e) {
			throw new UnxException(e);
		}
	}
	
	// List the files under the current path
	private List<FileResult> listFiles(Path path) throws IOException {
		List<FileResult> ret = new LinkedList<>();
		
		if (all) {
			ret.add(new FileResult(path.toFile()));
		}
		if (all && path.getParent() != null) {
			ret.add(new FileResult(path.getParent().toFile()));
		}
		for (File f: path.toFile().listFiles(new ListDirectoryFilter(path.toFile()))) {
			ret.add(new FileResult(f, recursive));
		}
		
		return ret;
	}
	
	// Presentation of a file result relative to some path (maybe nested)
	private void printFileResult(PrintWriter out, Path path, FileResult f) 
			throws IOException {
		if (f.getChildren() == null) {
			printFile(out, f, path);
		}
		else {
			out.println(f.getFile().getName() + ":");
			for (FileResult child: f.getChildren()) {
				printFileResult(out, path, child);
			}
		}
	}

	// Prints the information of a file
	private void printFile(PrintWriter out, FileResult f, Path currentPath) 
			throws IOException {
		String fileName = f.getFile().getName();
		if (currentPath.toFile().getCanonicalPath().equals(f.getFile().getCanonicalPath())) {
			fileName = ".";
		}
		else if (currentPath.toFile().getParentFile() != null &&
			currentPath.toFile().getParentFile().getCanonicalPath().equals(
					f.getFile().getCanonicalPath())) {
			fileName = "..";
		}
		out.println(fileName);
	}
	
	// Inner class to store the results
	private class FileResult {
		// File or directory
		private File f = null;
		// Children if f is a directory
		private List<FileResult> children = null;
		
		// Builds a file result on a File
		public FileResult(File f) throws IOException {
			this(f, false);
		}
		
		// Builds a file result on a File
		public FileResult(File f, boolean recursive) throws IOException {
			this.f = f;
			if (recursive && this.f.isDirectory()) {
				this.children = new LinkedList<>();
				for (File child: this.f.listFiles(new ListDirectoryFilter(f))) {
					this.children.add(new FileResult(child, recursive));
				}
			}
		}

		/**
		 * @return the f
		 */
		public File getFile() {
			return f;
		}

		/**
		 * @return the children
		 */
		public List<FileResult> getChildren() {
			return children;
		}					
	}	
	
	// Kind of filter closure implementation
	private interface Filter {		
		boolean accept(File f) throws IOException ;
	}
	
	// Simple filter chain implementation
	private static class FilterChain {
		private List<Filter> filters;
		public FilterChain() {
			filters = new LinkedList<>();
		}
		public void append(Filter f) {
			filters.add(f);
		}
		public boolean accept(File f) throws IOException  {
			boolean ret = true;
			Iterator<Filter> it = filters.iterator();
			while (ret && it.hasNext()) {
				ret &= it.next().accept(f);
			}
			return ret;
		}
	}
	
	// Implements a directory filter based on the options collected on
	//	ListDirectoryCommand.this
	private class ListDirectoryFilter implements FileFilter {
	
		//---------------------------------------------------------
		// Filter properties
		
		// Filter chain
		private FilterChain chain;
		
		//---------------------------------------------------------
		// Filter methods		
		
		/**
		 * Builds a filter based on the command line options.
		 * @param options Options passed to the filter.
		 */
		public ListDirectoryFilter(final File currentPath) throws IOException {
			// Build a filter chain
			chain = new FilterChain();
			// Fill in the filter chain
			if (!all && !almostAll) {
				chain.append(new Filter() {
					@Override
					public boolean accept(File f) throws IOException {
						return !f.getName().startsWith(".");
					}
				});
			}
			if (ignoreBackups) {
				chain.append(new Filter() {
					@Override
					public boolean accept(File f) throws IOException {
						return !f.getName().endsWith("~");
					}
				});
			}
			
			if (!all && !almostAll && hide != null && hide.trim().length() > 0) {
				final Pattern pattern = Pattern.compile(hide);
				// Improvement: try to solve this without repeating any code
				// Requirements for final pattern variable in order to use
				//	it into an anonymous implementation of Filter makes it 
				//	hard
				chain.append(new Filter() {
					@Override
					public boolean accept(File f) throws IOException {
						Matcher m = pattern.matcher(f.getName());
						return !m.find();
					}
				});
			}
			if (ignore != null && ignore.trim().length() > 0) {
				final Pattern pattern = Pattern.compile(ignore);				
				chain.append(new Filter() {
					@Override
					public boolean accept(File f) throws IOException {
						Matcher m = pattern.matcher(f.getName());
						return !m.find();
					}
				});
			}
		}
		
		@Override
		public boolean accept(File file) {
			boolean ret = true;
			try {
				ret = chain.accept(file);
			}
			catch(IOException ioe) {
				ret = false;
			}
			return ret;
		}
	}

	// Setters methods for the properties
	
	/**
	 * @param all the all to set
	 */
	public void setAll(Boolean all) {
		this.all = all;
	}

	/**
	 * @param almostAll the almostAll to set
	 */
	public void setAlmostAll(Boolean almostAll) {
		this.almostAll = almostAll;
	}

	/**
	 * @param ignoreBackups the ignoreBackups to set
	 */
	public void setIgnoreBackups(Boolean ignoreBackups) {
		this.ignoreBackups = ignoreBackups;
	}

	/**
	 * @param groupDirectoriesFirst the groupDirectoriesFirst to set
	 */
	public void setGroupDirectoriesFirst(Boolean groupDirectoriesFirst) {
		this.groupDirectoriesFirst = groupDirectoriesFirst;
	}

	/**
	 * @param hide the hide to set
	 */
	public void setHide(String hide) {
		this.hide = hide;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(Boolean color) {
		this.color = color;
	}

	/**
	 * @param ignore the ignore to set
	 */
	public void setIgnore(String ignore) {
		this.ignore = ignore;
	}

	/**
	 * @param humanReadable the humanReadable to set
	 */
	public void setHumanReadable(Boolean humanReadable) {
		this.humanReadable = humanReadable;
	}

	/**
	 * @param recursive the recursive to set
	 */
	public void setRecursive(Boolean recursive) {
		this.recursive = recursive;
	}

	/**
	 * @param longOutputFormat the longOutputFormat to set
	 */
	public void setLongOutputFormat(Boolean longOutputFormat) {
		this.longOutputFormat = longOutputFormat;
	}
}
