package ru.futurelink.mo.orm.exceptions;

public class OpenException extends Exception {

	private static final long serialVersionUID = 1L;

	public OpenException(String id, String message, Throwable ex) {
		super("Элемент с ID="+id+": "+message, ex);		
	}
}
