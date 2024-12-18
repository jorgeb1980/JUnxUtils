package unxutils.coreutils;

import cli.ANSIEscapeCode;
import cli.annotations.Command;
import cli.annotations.OptionalArgs;
import cli.annotations.Parameter;
import cli.annotations.Run;
import lombok.Getter;
import lombok.Setter;
import unxutils.format.HumanReadableFormat;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Boolean.FALSE;
import static java.lang.System.out;
import static java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.GROUP_READ;
import static java.nio.file.attribute.PosixFilePermission.GROUP_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;
import static java.util.Locale.ENGLISH;
import static unxutils.format.Format.format;

/**
<b>Program documentation</b><br>
<pre>
{@code
The ls program lists information about files (of any type, including directories). 
Options and file arguments can be intermixed arbitrarily, as usual.

For non-option command-line arguments that are directories, by default ls lists 
the contents of directories, not recursively, and omitting files with names 
beginning with .. For other non-option arguments, by default ls lists just the 
file name. If no non-option argument is specified, ls operates on the current 
directory, acting as if it had been invoked with a single argument of ..

By default, the output is sorted alphabetically, according to the locale settings 
in effect.3 If standard output is a terminal, the output is in columns (sorted 
vertically) and control characters are output as question marks; otherwise, the 
output is listed one per line and control characters are output as-is.

Because ls is such a fundamental program, it has accumulated many options over 
the years. They are described in the subsections below; within each section, 
options are listed alphabetically (ignoring case). The division of options into 
the subsections is not absolute, since some options affect more than one aspect 
of lss operation.

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
in directories it ignores files whose names start with ..

-a
--all
In directories, do not ignore file names that start with ..

-A
--almost-all
In directories, do not ignore all file names that start with .; ignore only . 
and ... The --all (-a) option overrides this option.

-B
--ignore-backups
In directories, ignore files that end with ~. This option is equivalent to 
--ignore='*~' --ignore='.*~'.

--group-directories-first
Group all the directories before the files and then sort the directories and the 
files separately using the selected sort key (see sort option). That is, this 
option specifies a primary sort key, and the sort option specifies a secondary 
key. However, any use of --sort=none (-U) disables this option altogether.

--hide=PATTERN
In directories, ignore files whose names match the shell pattern pattern, unless 
the --all (-a) or --almost-all (-A) is also given. This option acts like 
--ignore=pattern except that it has no effect if --all (-a) or --almost-all (-A) 
is also given.

This option can be useful in shell aliases. For example, if lx is an alias for 
ls --hide='*~' and ly is an alias for ls --ignore='*~', then the command 
lx -A lists the file README~ even though ly -A would not.

-I pattern
--ignore=pattern
In directories, ignore files whose names match the shell pattern (not regular 
expression) pattern. As in the shell, an initial . in a file name does not 
match a wildcard at the start of pattern. Sometimes it is useful to give this 
option several times. For example,

$ ls --ignore='.??*' --ignore='.[^.]' --ignore='#*'
The first option ignores names of length 3 or more that start with ., the 
second ignores all two-character names that start with . except .., and 
the third ignores names that start with #.

`-l'
`--format=long'
`--format=verbose'
     In addition to the name of each file, print the file type,
     permissions, number of hard links, owner name, group name, size in
     bytes, and timestamp (by default, the modification time).  For
     files with a time more than six months old or in the future, the
     timestamp contains the year instead of the time of day.  If the
     timestamp contains today's date with the year rather than a time
     of day, the file's time is in the future, which means you probably
     have clock skew problems which may break programs like `make' that
     rely on file times.

     For each directory that is listed, preface the files with a line
     `total BLOCKS', where BLOCKS is the total disk allocation for all
     files in that directory.  The block size currently defaults to 1024
     bytes, but this can be overridden (Note: Block size).  The
     BLOCKS computed counts each hard link separately; this is arguably
     a deficiency.

     The permissions listed are similar to symbolic mode specifications
     (Note: Symbolic Modes).  But `ls' combines multiple bits into the
     third character of each set of permissions as follows:
    `s'
          If the setuid or setgid bit and the corresponding executable
          bit are both set.

    `S'
          If the setuid or setgid bit is set but the corresponding
          executable bit is not set.

    `t'
          If the sticky bit and the other-executable bit are both set.

    `T'
          If the sticky bit is set but the other-executable bit is not
          set.

    `x'
          If the executable bit is set and none of the above apply.

    `-'
          Otherwise.

     Following the permission bits is a single character that specifies
     whether an alternate access method applies to the file.  When that
     character is a space, there is no alternate access method.  When it
     is a printing character (e.g., `+'), then there is such a method.

-R
--recursive
List the contents of all directories recursively.

 --color       colors the output
}
</pre>
*/
@Command(command="ls", description="List information about the files (the current directory by default).")
public class ListDirectoryCommand {

	//-----------------------------------------------------------------
	// Command constants

	// File size length, for long output format
	private static final int SIZE_LENGTH = 8;
	// Owner name length, for long output format
	private static final int OWNER_LENGTH = 12;
	// Maximum hard links number to be shown
	private static final Long MAX_HARD_LINKS = 999L;
	// English months date format
	private static final DateFormat MODIFICATION_MONTH_FORMAT = new SimpleDateFormat("MMM", ENGLISH);
	// Hours and minutes format
	private static final DateFormat MODIFICATION_TIME_FORMAT = new SimpleDateFormat("HH:mm", ENGLISH);
	// Windows executable files pattern
	private static final Pattern EXECUTABLE_FILES_PATTERN =
		Pattern.compile("([\\.\\w]+)\\.exe|([\\.\\w]+)\\.bat|([\\.\\w]+)\\.cmd|([\\.\\w]+)\\.scr");
	// Executable file (Windows)
	private static final String WINDOWS_EXECUTABLE_PERMISSIONS = "rwxrwxrwx";
	// Non executable file (Windows)
	private static final String WINDOWS_NONEXECUTABLE_PERMISSIONS = "rw-rw-rw-";

	//-----------------------------------------------------------------
	// Command parameters

	@Setter
	@Parameter(name="a",
		longName="all",
		description="In directories, do not ignore file names that start with ."
	)
	private Boolean all = FALSE;

	@Setter
	@Parameter(name="A",
		longName="almost-all",
		description="In directories, do not ignore all file names that start "
			+ "with .; ignore only . and .. The --all (-a) "
			+ "option overrides this option."
	)
	private Boolean almostAll = FALSE;

	@Setter
	@Parameter(name="B",
		longName="ignore-backups",
		description="In directories, ignore files that end with ~. This option "
			+ "is equivalent to --ignore='*~' --ignore='.*~'"
	)
	private Boolean ignoreBackups = Boolean.FALSE;

	@Setter
	@Parameter(longName="group-directories-first",
		description="Group all the directories before the files and then sort "
			+ "the directories and the files separately using the "
			+ "selected sort key (see sort option). That is, this "
			+ "option specifies a primary sort key, and the sort "
			+ "option specifies a secondary key. However, any use of "
			+ "sort=none (-U) disables this option altogether"
	)
	private Boolean groupDirectoriesFirst = FALSE;

	@Setter
	@Parameter(longName="hide",
		description="In directories, ignore files whose names match the shell pattern pattern,"
			+ " unless the --all (-a) or --almost-all (-A) is also given. This option "
			+ "acts like --ignore=pattern except that it has no effect if --all (-a) "
			+ "or --almost-all (-A) is also given"
	)
	private String hide = null;

	@Setter
	@Parameter(longName="color", description="colors the output.")
	private Boolean color = FALSE;

	@Setter
	@Parameter(name="I", longName="ignore", description="do not list implied entries matching shell PATTERN")
	private String ignore = null;

	@Setter
	@Parameter(name="h", longName="human-readable", description="print human readable sizes (e.g., 1K 234M 2G)")
	private Boolean humanReadable = FALSE;

	@Setter
	@Parameter(name="R", longName="recursive", description="list subdirectories recursively")
	private Boolean recursive = FALSE;

	@Setter
	@Parameter(name="l", description="use a long listing format")
	private Boolean longOutputFormat = FALSE;

	@Setter
	@OptionalArgs(name="FILE")
	private List<String> files;

	//-----------------------------------------------------------------
	// Command variables

	// Reported directory headers
	private final List<Path> reportedDirectoryPaths = new LinkedList<>();

	//-----------------------------------------------------------------
	// Command methods

	/**
	 * Builds an ls command.
	 */
	public ListDirectoryCommand() {
	}

	@Run
	// Entry point for ls
	public int execute(Path cwd) throws Exception {
		var paths = new LinkedList<Path>();
		if (files == null) {
			paths.add(cwd);
		} else {
			for (String file: files) {
				var path = Path.of(file);
				if (path.isAbsolute()) paths.add(path);
				else paths.add(new File(cwd.toFile(), file).toPath());
			}
		}
		for (var path: paths) {
			try {
				// Render the result presentation, combining the appropriated output options
				for (var f: listFiles(path)) printFileResult(paths.size() > 1, path, cwd, f);
			} catch(AccessDeniedException e) {
				// Cannot enter here...
				System.err.println(e.getMessage());
			}
		}
		return 0;
	}

	// List the files under the current path
	private List<FileResult> listFiles(Path path) throws IOException {
		var ret = new LinkedList<FileResult>();
		if (path.toFile().exists()) {
			if (path.toFile().isDirectory()) {
				if (all) {
					ret.add(new FileResult(path.toFile()));
				}
				if (all && path.getParent() != null) {
					ret.add(new FileResult(path.getParent().toFile()));
				}
				for (File f : path.toFile().listFiles(new ListDirectoryFilter())) {
					ret.add(new FileResult(f, recursive));
				}
			}
			else ret.add(new FileResult(path.toFile()));
		}
		return ret;
	}

	// Calculates newPath relative to originalPath, if possible
	// getRelativePath(/etc, /etc/openssh) -> openssh
	// getRelativePath(/etc, /opt/ibm) -> /opt/ibm
	private String getRelativePath(Path originalPath, Path newPath) {
		var ret = newPath.toString();
		if (newPath.toString().contains(originalPath.toString())) {
			ret = newPath.toString().replace(originalPath.toString(), "");
			// Remove non character at the begin
			if (ret.startsWith(FileSystems.getDefault().getSeparator())) {
				ret = ret.substring(1);
			}
		}
		return ret;
	}

	// Presentation of a file result relative to some path (maybe nested)
	private void printFileResult(boolean manyFiles, Path path, Path currentPath, FileResult f)
		throws IOException {
		// We write only once every directory header
		if (manyFiles
			&& currentPath.toFile().isDirectory()
			&& !reportedDirectoryPaths.contains(path)) {
			out.println(getRelativePath(currentPath, path) + ":");
			reportedDirectoryPaths.add(path);
		}
		if (f.getChildren() == null) {
			printFile(f, path);
		} else {
			for (FileResult child: f.getChildren()) {
				printFileResult(true, path, currentPath, child);
			}
		}
	}

	// Prints the information of a file
	private void printFile(FileResult f, Path currentPath) throws IOException {
		var posixAttrs = f.getPosixAttrs();
		var fileName = getFileName(f, currentPath, posixAttrs);
		// Is it long?
		if (longOutputFormat) {
			// Long output format:
            /*
			 	1		file type (d/l/-)
			 	9		permission mask
				-1-
				3		number of hard links
				-1-
				OWNER_LENGTH		owner name
				-1-
				OWNER_LENGTH		owner's group name
				-1-
				SIZE_LENGTH		file size
				-1-
				3		month of last modification date
				-1-
				2		day of last modification date
				-1-
				5		year of last modification date if not the same; hour if the same
				-1-
				X		name of the file
			 */
			var basicAttrs = f.getBasicAttrs();
			var fileOwnerAttrs = f.getFileOwnerAttributeView();

			var sb = new StringBuilder();
			// File type
			sb.append(f.getFile().isDirectory()?"d":(basicAttrs.isSymbolicLink()?"l":"-"));
			// Permissions
			if (posixAttrs != null) {
				Set<PosixFilePermission> p = posixAttrs.permissions();
				sb.append(p.contains(OWNER_READ)?"r":"-");
				sb.append(p.contains(OWNER_WRITE)?"w":"-");
				sb.append(p.contains(OWNER_EXECUTE)?"x":"-");
				sb.append(p.contains(GROUP_READ)?"r":"-");
				sb.append(p.contains(GROUP_WRITE)?"w":"-");
				sb.append(p.contains(GROUP_EXECUTE)?"x":"-");
				sb.append(p.contains(OTHERS_READ)?"r":"-");
				sb.append(p.contains(OTHERS_WRITE)?"w":"-");
				sb.append(p.contains(OTHERS_EXECUTE)?"x":"-");
			} else {
				// Assume Windows:
				// rwxrwxrwx for executables
				// rw-rw-rw- for the rest
				var matcher = EXECUTABLE_FILES_PATTERN.matcher(f.getFile().getName());
				if (matcher.matches()) {
					sb.append(WINDOWS_EXECUTABLE_PERMISSIONS);
				} else {
					sb.append(WINDOWS_NONEXECUTABLE_PERMISSIONS);
				}

			}
			// Space
			sb.append(" ");
			// Number of hard links
			var hardLinks = f.getHardLinks();
			if (hardLinks.compareTo(MAX_HARD_LINKS) > 0) {
				hardLinks = MAX_HARD_LINKS;
			}
			// Space
			sb.append(" ");
			// Owner name
			if (posixAttrs != null) {
				sb.append(format(posixAttrs.owner().getName(), OWNER_LENGTH));
			} else {
				sb.append(format(fileOwnerAttrs.getOwner().getName(), OWNER_LENGTH));
			}
			// Space
			sb.append(" ");
			// Owner's group name
			if (posixAttrs != null) {
				sb.append(format(posixAttrs.group().getName(), OWNER_LENGTH));
			} else {
				sb.append(format(fileOwnerAttrs.getOwner().getName(), OWNER_LENGTH));
			}
			// Space
			sb.append(" ");
			// Size
			sb.append(format(getSize(basicAttrs.size()), SIZE_LENGTH));
			// Space
			sb.append(" ");
			// Last modification date
			sb.append(formatModificationDate(basicAttrs.lastModifiedTime()));
			// Space
			sb.append(" ");
			// File name
			sb.append(fileName);
			out.println(sb.toString());
		} else {
			out.println(fileName);
		}
	}

	// Renders the name of the file
	private String getFileName(FileResult f, Path currentPath, PosixFileAttributes posixAttrs)
		throws IOException {
		var fileName = f.getFile().getName();
		if (f.getFile().isDirectory() && currentPath.toFile().getCanonicalPath().equals(f.getFile().getCanonicalPath())) {
			fileName = ".";
		} else if (currentPath.toFile().getParentFile() != null &&
			currentPath.toFile().getParentFile().getCanonicalPath().equals(
				f.getFile().getCanonicalPath())) {
			fileName = "..";
		}
		// Should we color it?
		if (color) {
			if (f.getFile().isDirectory()) {
				fileName = ANSIEscapeCode.paint(fileName, ANSIEscapeCode.BRIGHT_BLUE) + "/";
			} else {
				boolean executable = false;
				if (posixAttrs != null) {
					var permissions = posixAttrs.permissions();
					executable =
						permissions.contains(OWNER_EXECUTE) |
							permissions.contains(GROUP_EXECUTE) |
							permissions.contains(OTHERS_EXECUTE);
				} else {
					var matcher = EXECUTABLE_FILES_PATTERN.matcher(f.getFile().getName());
					executable = matcher.matches();
				}
				if (executable) {
					fileName = ANSIEscapeCode.paint(fileName, ANSIEscapeCode.BRIGHT_GREEN);
				}
			}
		}
		return fileName;
	}

	// Properly formats the modification date for presentation
	private String formatModificationDate(FileTime lastModifiedTime) {
        /*
			3		month of last modification date
			-1-
			2		day of last modification date
			-1-
			5		year of last modification date if not the same; hour if the same 
		 */
		var sb = new StringBuilder();
		var modificationTime = Calendar.getInstance();
		modificationTime.setTimeInMillis(lastModifiedTime.toInstant().toEpochMilli());
		var rightNow = Calendar.getInstance();
		rightNow.setTime(new Date());
		// Month in english
		sb.append(MODIFICATION_MONTH_FORMAT.format(modificationTime.getTime()));
		sb.append(" ");
		sb.append(format(Integer.toString(modificationTime.get(Calendar.DAY_OF_MONTH)), 2));
		sb.append(" ");
		// Year if not the same that right now
		if (modificationTime.get(Calendar.YEAR) != rightNow.get(Calendar.YEAR)) {
			sb.append(format(Integer.toString(modificationTime.get(Calendar.YEAR)), 5));
		} else {
			// Hour of modification
			sb.append(format(MODIFICATION_TIME_FORMAT.format(modificationTime.getTime()), 5));
		}

		return sb.toString();
	}

	// Gets the file size
	private String getSize(long size) {
		var ret = Long.toString(size);
		if (humanReadable) {
			ret = HumanReadableFormat.format(size);
		}
		return ret;
	}

	// Inner class to store the results
	private class FileResult {
		// File or directory
		private File f = null;
		@Getter
        private List<FileResult> children = null;
		// POSIX attributes
		private PosixFileAttributes posixAttrs = null;
		// Basic attributes
		private BasicFileAttributes basicAttrs = null;
		// Hard links
		private Long hardLinks = null;
		// File owner attributes
		private FileOwnerAttributeView fileOwnerAttrs = null;

		// Builds a file result on a File
		public FileResult(File f) throws IOException {
			this(f, false);
		}

		// Builds a file result on a File
		public FileResult(File f, boolean recursive) throws IOException {
			this.f = f;
			if (recursive && this.f.isDirectory()) {
				this.children = new LinkedList<>();
				for (File child: this.f.listFiles(new ListDirectoryFilter())) {
					this.children.add(new FileResult(child, recursive));
				}
			}
		}

		// Gathers the posix attributes for the long output format
		public PosixFileAttributes getPosixAttrs() {
			if (posixAttrs == null) {
				try {
					posixAttrs = Files.getFileAttributeView(f.toPath(), PosixFileAttributeView.class)
						.readAttributes();
				} catch(Exception ioe) {
					// Just assume that the current file store does not support
					//	POSIX attributes
					posixAttrs = null;
				}
			}
			return posixAttrs;
		}

		// Gathers the file owner permissions
		public FileOwnerAttributeView getFileOwnerAttributeView() {
			if (fileOwnerAttrs == null) {
				fileOwnerAttrs = Files.getFileAttributeView(f.toPath(), FileOwnerAttributeView.class);
			}
			return fileOwnerAttrs;
		}

		// Gathers the basic attributes for the long output format
		public BasicFileAttributes getBasicAttrs() {
			if (basicAttrs == null) {
				try {
					basicAttrs = Files.getFileAttributeView(f.toPath(), BasicFileAttributeView.class)
						.readAttributes();
				} catch(IOException ioe) {
					ioe.printStackTrace();
				}
			}
			return basicAttrs;
		}

		// Gathers the number of hard links for the long output format
		public Long getHardLinks() {
			if (hardLinks == null) {
				try {
					hardLinks = Long.valueOf(
						java.nio.file.Files.getAttribute(f.toPath(),
							"unix:nlink").toString());
				} catch(Exception e) {
					hardLinks = 1l;
				}
			}
			return hardLinks;
		}

		/**
		 * @return the f
		 */
		public File getFile() {
			return f;
		}

    }

	// Kind of filter closure implementation
	private interface Filter {
		boolean accept(File f) throws IOException ;
	}

	// Simple filter chain implementation
	private static class FilterChain {
		private final List<Filter> filters;

		public FilterChain() {
			filters = new LinkedList<>();
		}

		public void append(Filter f) {
			filters.add(f);
		}

		public boolean accept(File f) throws IOException  {
			boolean ret = true;
			var it = filters.iterator();
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
		private final FilterChain chain;

		//---------------------------------------------------------
		// Filter methods

		/**
		 * Builds a filter based on the command line options.
		 */
		public ListDirectoryFilter() throws IOException {
			// Build a filter chain
			chain = new FilterChain();
			// Fill in the filter chain
			if (!all && !almostAll) {
				chain.append(f -> !f.getName().startsWith("."));
			}
			if (ignoreBackups) {
				chain.append(f -> !f.getName().endsWith("~"));
			}

			if (!all && !almostAll && hide != null && !hide.trim().isEmpty()) {
				final var pattern = Pattern.compile(hide);
				// Improvement: try to solve this without repeating any code
				// Requirements for final pattern variable in order to use
				//	it into an anonymous implementation of Filter makes it
				//	hard
				chain.append(f -> {
                    Matcher m = pattern.matcher(f.getName());
                    return !m.find();
                });
			}
			if (ignore != null && !ignore.trim().isEmpty()) {
				final Pattern pattern = Pattern.compile(ignore);
				chain.append(f -> {
                    var m = pattern.matcher(f.getName());
                    return !m.find();
                });
			}
		}

		@Override
		public boolean accept(File file) {
			boolean ret = true;
			try {
				ret = chain.accept(file);
			} catch(IOException ioe) {
				ret = false;
			}
			return ret;
		}
	}

}
