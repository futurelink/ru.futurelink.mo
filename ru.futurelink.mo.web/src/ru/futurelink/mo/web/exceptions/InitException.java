/**
 * 
 */
package ru.futurelink.mo.web.exceptions;

/**
 * @author pavlov
 *
 */
public class InitException extends Exception {
	private static final long serialVersionUID = 1L;

	public InitException(String message) {
		super(message);
	}
	
	public InitException(String message, Throwable e) {
		super(message, e);
	}
	
}
