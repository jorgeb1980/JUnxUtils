package unxutils.common;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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
		Date startEntryPoint = new Date();
		int ret = 0;
		try {
			Path currentPath = Paths.get("").toAbsolutePath();	
			String command = head(args);
			List<String> rest = tail(args);
			// Special case: when asked for --help, print the help straight now
			if (rest.size() == 1 && rest.contains("--help")) {
				printHelp(command);
			}
			else {
				EntryPoint entry = new EntryPoint();
				ret = entry.executeEntryPoint(command, rest, currentPath);
				entry.flush(false);
			}
		}
		catch(UnxException unxe) {
			unxe.printStackTrace();
			System.err.println(unxe.getMessage());
			System.exit(unxe.getReturnCode());
		}
		Date endEntryPoint = new Date();
		printTime(startEntryPoint, endEntryPoint, "Total time");
		// The command returned some exit code, this is our return code
		System.exit((Integer) ret);			
	}
	
	// Prints the help generated by Apache Commons Cli for the specified
	//	command
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static void printHelp(String commandClassName) throws UnxException {
		Class commandClass = lookForCommand(commandClassName);
		if (commandClass == null) {
			throw new UnxException(String.format("Command %s not recognized", commandClassName));
		}
		Options options = buildOptions(commandClass);
		String optionalArg = lookForOptionalArgs(commandClass);
		String commandName = 
				((Command)commandClass.getAnnotation(Command.class)).command();
		String commandDescription = 
				((Command)commandClass.getAnnotation(Command.class)).description();
		String footer = "\nJUnxUtils v%s";
		 
		HelpFormatter formatter = new HelpFormatter();
		if (optionalArg != null) {
			// Format the usage in order to show a list of arguments if necessary
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(baos, Charset.forName("utf-8"));
			PrintWriter pwTemp = new PrintWriter(writer);
			formatter.printUsage(pwTemp, 120, "ls", options);
			pwTemp.flush();
			StringBuilder sb = new StringBuilder(new String(baos.toByteArray(), Charset.forName("utf-8")));
			sb.append(String.format(" [%s]...", optionalArg));
			String usage = sb.toString().replaceAll("[\r\n]", "");
			usage = usage.replace("usage:", "");
			formatter.printHelp(
				usage, 
				commandDescription, 
				options, 
				String.format(footer, getUnxUtilsVersion()), 
				false);
		}
		else {
			formatter.printHelp(
				commandName, 
				commandDescription, 
				options, 
				String.format(footer, getUnxUtilsVersion()), 
				true);
		}
	}

	// Looks for an OptionalArgs annotation; it means that the command accepts
	//	additional arguments such as ls or df.
	// It returns the name argument of the OptionalArgs annotation or null if
	//	not found
	private static String lookForOptionalArgs(@SuppressWarnings("rawtypes") Class commandClass) {
		Field fields[] = commandClass.getDeclaredFields();
		String ret = null;
		for (Field field: fields) {
			if (field.isAnnotationPresent(OptionalArgs.class)) {
				OptionalArgs annotation = field.getAnnotation(OptionalArgs.class);
				ret = annotation.name();
			}
		}
		return ret;
	}

	// Gets the current application version from the version.properties file
	private static String getUnxUtilsVersion() {
		String ret = "";
		try (InputStream is = EntryPoint.class.getClassLoader().
				getResourceAsStream("junxutils.properties")) {
			Properties prop = new Properties();
			prop.load(is);
			ret = prop.getProperty("junxutils.version");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return ret;
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
	
	private static void printTime(Date initial, Date ending, String message) {
		//System.err.println(message + " -> " + (ending.getTime() - initial.getTime()) + " mseg");
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
			Date beginLookForCommand = new Date();
			Class<?> commandClass = lookForCommand(command);
			//Class<?> commandClass = Class.forName(command);
			Date endLookForCommand = new Date();
			printTime(beginLookForCommand, endLookForCommand, "looking for command");
			// Instantiate the proper command
			Object theCommand = instantiateCommand(commandClass);
			Date endInstantiateCommand = new Date();
			printTime(endLookForCommand, endInstantiateCommand, "instantiate command");
			// Look for an 'execute' method with the next arguments:
			// Path - current path of the command
			// PrintWriter - standard output
			// PrintWriter - error output
			Method execute = findExecuteMethod(commandClass);
			Date endFindExecuteMethod = new Date();
			printTime(endInstantiateCommand, endFindExecuteMethod, "find execute method");
			ret = executeCommand(theCommand, execute, currentPath, commandArguments);
			Date endExecuteCommand = new Date();
			printTime(endFindExecuteMethod, endExecuteCommand, "execute command");
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
		Method optionalArgsMethod = null;
		for (Field field: fields) {			
			Method method = lookForSetter(field, methods);
			if (field.isAnnotationPresent(Parameter.class)) {
				Parameter parameter = field.getAnnotation(Parameter.class);
				if (parameter.name().trim().length() != 0) {
					methodsMap.put(parameter.name(), method);
				}
				if (parameter.longName().trim().length() != 0) {
					methodsMap.put(parameter.longName(), method);
				}
				
			}
			else if (method != null && field.isAnnotationPresent(OptionalArgs.class)) {
				optionalArgsMethod = method;
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
		if (optionalArgsMethod != null) {
			// The method must exist and admit a list of strings as its only
			//	parameter
			List<String> args = Arrays.asList(commandLine.getArgs());
			if (args != null && args.size() > 0) {
				optionalArgsMethod.invoke(command, args);
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
	public static Options buildOptions(Class<? extends Object> commandClass) {
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
						parameter.longName().trim().length()==0?parameter.name():parameter.longName(), 
						parameter.hasArg(), 
						parameter.description());
				}
			}
		}
		return options;
	}

	// Looks for the proper command class
	// The search with google reflections has been optimized away to the gradle build process 
	//	in order to get an improvement of ~0.15 sec for each program call.
	@SuppressWarnings("rawtypes")
	private static Class lookForCommand(String commandClassName) 
			throws UnxException {
		Class ret = null;
		try {
			ret = Class.forName(commandClassName);
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new UnxException(e);
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

	/** Returns the first argument */
	public static String head(String[] args) {
		String ret = null;
		if (args !=  null && args.length > 0) {
			ret = args[0];
		}
		return ret;
	}
	
	/** Returns the second and later arguments*/
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
