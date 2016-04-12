package unxutils.common;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

/**
 * Common launcher for all the commands. 
 */
public class EntryPoint {

	//------------------------------------------------------------
	// Class methods
	
	/**
	 * Entry point for the command launcher.
	 * @param args Command arguments
	 */
	public static void main(String[] args) {
		Path currentRelativePath = Paths.get("").toAbsolutePath();	
		String command = head(args);
		List<String> rest = tail(args);
		if (command != null) {
			Set<Class<?>> commands = 
				new Reflections("unxutils").getTypesAnnotatedWith(Command.class);
			Class<?> commandClass = lookForCommand(commands, command);
			// Instantiate the proper command
			if (commandClass != null) {
				Object theCommand = null;
				try {
					theCommand = commandClass.getConstructor().newInstance();				
				}
				catch (Exception e) {
					System.err.println("Could not instantiate the command");
					System.exit(-1337);
				}
				// Look for an 'execute' method with the next arguments:
				// Path 
				// List<String> 
				Method execute = null;
				try {
					execute = commandClass.getDeclaredMethod("execute", Path.class, List.class);
				}
				catch(Exception e) {
					System.err.println(
						String.format("The class %s has not a proper execute method", 
								commandClass.getName()));
					System.exit(-1337);
				}
				Object ret = null;
				try {
					// Launch the command
					ret = execute.invoke(theCommand, currentRelativePath, rest);
				}
				catch(Throwable t) {
					System.err.println(t.getMessage());
					System.exit(-1337);
				}
				// The command returned some exit code, this is our return code
				System.exit((Integer) ret);
			}
			else {
				System.err.println("Could not instantiate the command");
				System.exit(-1337);
			}
		}
	}
	
	// Looks for the proper command class
	private static Class lookForCommand(Set<Class<?>> commands, String command) {
		Class<?> ret = null;
		Iterator<Class<?>> it = commands.iterator();
		while (it.hasNext() && ret == null) {
			Class<?> clazz = it.next();
			Command annotation = clazz.getAnnotation(Command.class);
			if (command.equals(annotation.command())) {
				ret = clazz;
			}

		}
		return ret;
	}

	// Returns the first argument
	public static String head(String[] args) {
		String ret = null;
		if (args !=  null && args.length > 0) {
			ret = args[0];
		}
		return ret;
	}
	
	// Returns the second and later arguments
	public static List<String> tail(String[] args) {
		List<String> ret  = new LinkedList<>();
		if (args != null && args.length > 1) {
			ret.addAll(Arrays.asList(args));
		}
		return ret;
	}
}
