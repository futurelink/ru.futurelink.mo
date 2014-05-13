package ru.futurelink.mo.orm.exceptions;

public class DTOException extends Exception {

	private static final long serialVersionUID = 1L;

	public DTOException(String string, Exception ex) {
		super(string, ex);
	}

}
