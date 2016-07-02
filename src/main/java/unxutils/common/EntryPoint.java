package unxutils.common;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.reflections.Reflections;

/**
 * Common launcher for all the commands. 
 */
public class EntryPoint {

	//------------------------------------------------------------
	// Class constants
	
	// Output encoding
	public static final Charset ENCODING = Charset.forName("UTF-8");
	
	//------------------------------------------------------------
	// Class properties
	
	// Program's own standardOutput
	private PrintWriter standardOutput;
	private ByteArrayOutputStream standardOutputBuffer;
	// Program's own errorOutput
	private PrintWriter errorOutput;
	private ByteArrayOutputStream errorOutputBuffer;
	
	//------------------------------------------------------------
	// Class methods	
	
	/**
	 * Entry point for the command launcher.
	 * @param args Command arguments
	 */
	public static void main(String[] args) {
		int ret = 0;
		try {
			Path currentPath = Paths.get("").toAbsolutePath();	
			String command = head(args);
			List<String> rest = tail(args);
			EntryPoint entry = new EntryPoint();
			ret = entry.executeEntryPoint(command, rest, currentPath);
			entry.flush(false);
		}
		catch(UnxException unxe) {
			System.err.println(unxe.getMessage());
			System.exit(unxe.getReturnCode());
		}
		// The command returned some exit code, this is our return code
		System.exit((Integer) ret);			
	}
	
	// Initializes an entry point with its proper standard and error output
	//	buffers
	public EntryPoint() {
		standardOutputBuffer = new ByteArrayOutputStream();
		standardOutput = new PrintWriter(
			new OutputStreamWriter(
				standardOutputBuffer, ENCODING));
		
		errorOutputBuffer = new ByteArrayOutputStream();
		errorOutput = new PrintWriter(
			new OutputStreamWriter(
				errorOutputBuffer, ENCODING));
	}
	
	/**
	 * @return the standardOutputBuffer
	 */
	public String getStandardOutputBuffer() {
		standardOutput.flush();
		return new String(standardOutputBuffer.toByteArray(), ENCODING);
	}

	/**
	 * @return the errorOutputBuffer
	 */
	public String getErrorOutputBuffer() {
		errorOutput.flush();
		return new String(errorOutputBuffer.toByteArray(), ENCODING);
	}

	/**
	 * Flush the content of the output buffers to the real standard
	 * and error output.
	 */
	public void flush(boolean error) {
		System.out.println(getStandardOutputBuffer());
		if (error) {
			System.err.println(getErrorOutputBuffer());
		}
	}
	
	/**
	 * Executes a command with certain parameters and path.
	 * @param command Name of the command to execute.
	 * @param commandArguments Arguments for the command.
	 * @param currentPath File path where the command is executed.
	 * @throws UnxException if any error is reached during the execution.
	 */
	public int executeEntryPoint(String command, List<String> commandArguments, Path currentPath) 
			throws UnxException {
		int ret = 0;
		if (command != null) {
			Set<Class<?>> commands = 
				new Reflections("unxutils").getTypesAnnotatedWith(Command.class);
			Class<?> commandClass = lookForCommand(commands, command);
			// Instantiate the proper command
			Object theCommand = instantiateCommand(commandClass);
			// Look for an 'execute' method with the next arguments:
			// Path - current path of the command
			// PrintWriter - standard output
			// PrintWriter - error output
			Method execute = findExecuteMethod(commandClass);
			ret = executeCommand(theCommand, execute, currentPath, commandArguments);
		}
		else {
			throw new UnxException("Please specify which command you wish to launch", -1);
		}
		return ret;
	}
	
	// Executes the line command, returning the exit code
	private Integer executeCommand(
			Object command, 
			Method execute, 
			Path currentPath,
			List<String> args) throws UnxException {
		Object ret;
		try {
			// Every command should have an execute method with:
			// + Current path for the command
			CommandLine commandLine =  new DefaultParser().parse(
				buildOptions(command.getClass()), 
				args.toArray(new String[args.size()]),
				// Fail if there is something unrecognized
				false);
			applyArguments(command, commandLine);
			// Apply the command line to the command
			ret = execute.invoke(command, currentPath, standardOutput, errorOutput);
		} 
		catch (IllegalAccessException 
				| IllegalArgumentException 
				| InvocationTargetException
				| ParseException e) {
			throw new UnxException(e).setReturnCode(-1337);
		}
		return (Integer) ret;
	}
	
	// This method applies every argument in the command line to the command object
	private void applyArguments(Object command, CommandLine commandLine) 
			throws IllegalAccessException, InvocationTargetException {
		// Map the setter methods to commandLine option names
		Method[] methods = command.getClass().getMethods();
		Field fields[] = command.getClass().getDeclaredFields();
		Map<String, Method> methodsMap = new HashMap<>();
		for (Field field: fields) {
			if (field.isAnnotationPresent(Parameter.class)) {
				Method method = lookForSetter(field, methods);
				if (method != null) {
					Parameter parameter = field.getAnnotation(Parameter.class);
					if (parameter.name().trim().length() != 0) {
						methodsMap.put(parameter.name(), method);
					}
					if (parameter.longName().trim().length() != 0) {
						methodsMap.put(parameter.longName(), method);
					}
				}
				else {
					System.err.println(
						String.format("The annotated field %s in the class %s has no setter method", 
							field.getName(), command.getClass().getName()));
				}
			}
		}
		// Apply the command line values to the indexed methods
		// Every method is supposed to be a simple setter with one argument, of
		//		one of the following types :
		//	String
		//	Integer
		//	Long
		//	Double
		//	Float
		// An argument-less parameter is supposed to be a Boolean, and if it is
		//	present, it is assumed to be true
		
		for (Option option: commandLine.getOptions()) {
			Method method = methodsMap.get(option.getArgName());
			if (method == null) {
				method = methodsMap.get(option.getLongOpt());
			}
			if (method != null) {
				method.invoke(command, processArgument(method, option.getValue()));
			}
		}
	}

	// Gets a string, sets all the string to lower case, then the first
	//	character to upper case.
	private String firstInUpperCase(String fieldName) {
		return fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
	}
	
	// Looks for the setter method of a class field
	private Method lookForSetter(Field field, Method[] methods) {
		Method ret = null;
		if (field != null && methods != null && methods.length > 0) {
			int i = 0;
			String expectedMethodName = "set" + firstInUpperCase(field.getName());
			while (ret == null && i < methods.length) {
				Method method = methods[i++];
				if (method.getName().equals(expectedMethodName)) {
					ret = method;
				}
			}
		}
		return ret;
	}

	// This method processes the option value in order to pass the right type
	//	to the command object
	private Object processArgument(Method method, String value) {
		Object ret = null;
		Class<?>[] types = method.getParameterTypes();
		if (types != null && types.length == 1) {
			Class<?> type = types[0];
			if (type.equals(String.class)) {
				ret = value;
			}
			else if (type.equals(Integer.class)) {
				ret = new Integer(value);
			}
			else  if (type.equals(Long.class)) {
				ret = new Long(value);
			}
			// This can be improved by looking closely at precision, etc.
			else  if (type.equals(Double.class)) {
				ret = new Double(value);
			}
			else  if (type.equals(Float.class)) {
				ret = new Float(value);
			}
			// Boolean arguments
			else if (type.equals(Boolean.class)) {
				// If value here is null or empty, it is assumed to be true
				if (value == null || value.trim().length() == 0) {
					ret = Boolean.TRUE;
				}
				else {
					ret = Boolean.valueOf(value);
				}
			}
		}
		return ret;
	}

	// This method build an Apache Command Line Options object upon
	//	the annotated parameters information in the class
	public Options buildOptions(Class<? extends Object> commandClass) {
		//Method[] methods = commandClass.getMethods();
		// It will be the fields that get annotated, and those fields will get
		//	us to the setter method
		Field fields[] = commandClass.getDeclaredFields();
		Options options = new Options();
		for (Field field: fields) {
			if (field.isAnnotationPresent(Parameter.class)) {
				Parameter parameter = field.getAnnotation(Parameter.class);
				// Look for the corresponding setter method
				//Method method = lookForSetter(field, methods);
				if (parameter.name().trim().length() == 0) {
					options.addOption(
						Option.builder().
							hasArg(parameter.hasArg()).
							desc(parameter.description()).
							longOpt(parameter.longName()).build());
				}
				else {
					options.addOption(
						parameter.name(), 
						parameter.longName(), 
						parameter.hasArg(), 
						parameter.description());
				}
			}
		}
		return options;
	}

	// Looks for the proper command class	
	@SuppressWarnings("rawtypes")
	private Class lookForCommand(Set<Class<?>> commands, String command) 
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
	private Object instantiateCommand(Class<?> commandClass) 
			throws UnxException {
		try {
			return commandClass.getConstructor().newInstance();
		} 
		catch (InstantiationException 
				| IllegalAccessException 
				| IllegalArgumentException 
				| InvocationTargetException
				| NoSuchMethodException 
				| SecurityException e) {
			throw new UnxException(e).setReturnCode(-1337);
		}		
	}
	
	// Finds an execute method in the command class
	private Method findExecuteMethod(Class<?> commandClass) 
			throws UnxException {
		try {
			return commandClass.
				getDeclaredMethod("execute", 
						Path.class, 
						PrintWriter.class, 
						PrintWriter.class);
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