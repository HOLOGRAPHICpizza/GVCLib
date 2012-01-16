package org.peak15.GVCLib;

/**
 * Exception thrown by GVC commands when they fail.
 */
public class GVCException extends Exception {

	private static final long serialVersionUID = 4416666289996577354L;
	
	/**
	 * Constructor for a generic exception.
	 */
	public GVCException() {
		super("Failed to run command.");
	}
	
	/**
	 * Constructor for a custom exception.
	 * @param message Message for the exception.
	 */
	public GVCException(String message) {
		super(message);
	}
}
