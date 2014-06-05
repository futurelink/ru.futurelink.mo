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
	
	private String mAccessData;
	
	public DTOAccessException(String string, Exception ex) {
		super(string, ex);
		
		mAccessData = null;
	}

	public void setAccessData(String data) {
		mAccessData = data;
	}
	
	public String getAccessData() {
		return mAccessData;
	}
	
	private static final long serialVersionUID = 1L;

}
