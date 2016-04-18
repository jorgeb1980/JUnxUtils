package unxutils.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
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
		try {
			Path currentPath = Paths.get("").toAbsolutePath();	
			String command = head(args);
			List<String> rest = tail(args);
			if (command != null) {
				Set<Class<?>> commands = 
					new Reflections("unxutils").getTypesAnnotatedWith(Command.class);
				Class<?> commandClass = lookForCommand(commands, command);
				// Instantiate the proper command
				Object theCommand = instantiateCommand(commandClass, rest);
				// Look for an 'execute' method with the next arguments:
				// Path 
				// List<String> 
				Method execute = findExecuteMethod(commandClass);
				Integer ret = executeCommand(theCommand, execute, currentPath);
				// The command returned some exit code, this is our return code
				System.exit((Integer) ret);			
			}
		}
		catch(UnxException unxe) {
			System.err.println(unxe.getMessage());
			System.exit(unxe.getReturnCode());
		}
	}
	
	// Executes the line command, returning the exit code
	private static Integer executeCommand(
			Object command, 
			Method execute, 
			Path currentPath) throws UnxException {
		Object ret;
		try {
			ret = execute.invoke(command, currentPath);
		} 
		catch (IllegalAccessException 
				| IllegalArgumentException 
				| InvocationTargetException e) {
			throw new UnxException(e).setReturnCode(-1337);
		}
		return (Integer) ret;
	}
	
	// Looks for the proper command class	
	@SuppressWarnings("rawtypes")
	private static Class lookForCommand(Set<Class<?>> commands, String command) 
			throws UnxException {
		Class<?> ret = null;
		Iterator<Class<?>> it = commands.iterator();
		while (it.hasNext() && ret == null) {
			Class<?> clazz = it.next();
			Command annotation = clazz.getAnnotation(Command.class);
			if (command.equals(annotation.command())) {
				ret = clazz;
			}

		}
		if (ret == null) {
			throw new UnxException("Could not instantiate the command").
				setReturnCode(-1337);
		}
		return ret;
	}
	
	// Instantiates a command object
	private static Object instantiateCommand(Class<?> commandClass, List<String> args) 
			throws UnxException {
		try {
			return commandClass.getConstructor(List.class).newInstance(args);
		} catch (InstantiationException 
				| IllegalAccessException 
				| IllegalArgumentException 
				| InvocationTargetException
				| NoSuchMethodException 
				| SecurityException e) {
			throw new UnxException(e).setReturnCode(-1337);
		}		
	}
	
	// Finds an execute method in the command class
	private static Method findExecuteMethod(Class<?> commandClass) 
			throws UnxException {
		try {
			return commandClass.getDeclaredMethod("execute", Path.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new UnxException(e).setReturnCode(-1337);
		}
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
			for (int i = 1; i < args.length; i++) {
				ret.add(args[i]);
			}
		}
		return ret;
	}
}
