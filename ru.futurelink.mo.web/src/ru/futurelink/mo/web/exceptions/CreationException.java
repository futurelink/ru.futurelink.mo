/**
 * 
 */
package ru.futurelink.mo.web.exceptions;

/**
 * @author pavlov
 *
 */
public class CreationException extends Exception {
	private static final long serialVersionUID = 1L;

	private String mMessage;
	
	public CreationException(String message) {
		super();
		
		mMessage = message;
	}

	public String getMessage() { return mMessage; }
	
}
