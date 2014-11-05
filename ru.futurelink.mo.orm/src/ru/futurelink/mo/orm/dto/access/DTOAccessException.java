/*******************************************************************************
 * Copyright (c) 2013-2014 Pavlov Denis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pavlov Denis - initial API and implementation
 ******************************************************************************/

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
