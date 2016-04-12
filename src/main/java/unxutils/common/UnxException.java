package unxutils.common;

/**
 * This class models a generic exception type for error handling through
 * the entire package.
 */
public class UnxException extends Exception {

	//------------------------------------------------------------
	// Class constants
	
	/**
	 * Used for serialization.
	 */
	private static final long serialVersionUID = -2396560778835143351L;

	//------------------------------------------------------------
	// Class methods
	
	/**
	 * Builds an exception with an error message.
	 * @param message Error message.
	 */
	public UnxException(String message) {
		super(message);
	}
	
	/**
	 * Builds an exception wrapping a possible cause.
	 * @param cause Cause of the exception.
	 */
	public UnxException(Exception cause) {
		super(cause);
	}
}
