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

package ru.futurelink.mo.orm.security;

import ru.futurelink.mo.orm.exceptions.SaveException;

/**
 * @author pavlov
 *
 */
public interface IUserParams {
	public void setUsecaseName(String usecaseName);
	public String getUsecaseName();
	
	public void setParamName(String paramName);
	public String getParamName();
	
	public void setUser(String userId);
	public String getUser();

	public void setParam(String name, Object value);
	public Object getParam(String name);

	public Object save() throws SaveException;
}
