/**
 * 
 */
package ru.futurelink.mo.orm.dto.access;

import ru.futurelink.mo.orm.exceptions.DTOException;

/**
 * @author pavlov
 *
 */
public class DTOAccessException extends DTOException {
	public DTOAccessException(String string, Exception ex) {
		super(string, ex);
	}

	private static final long serialVersionUID = 1L;

}
