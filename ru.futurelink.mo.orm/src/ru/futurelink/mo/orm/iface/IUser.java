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

package ru.futurelink.mo.orm.iface;

import java.util.TimeZone;

/**
 * @author pavlov
 *
 */
public interface IUser extends ICommonObject {

	public void setEmail(String email);
	public String getEmail();
	
	/**
	 * User name
	 */
	public void setUserName(String userName);
	public String getUserName();
	
	/**
	 * User login
	 */
	public void setLogin(String login);
	public String getLogin();

	/**
	 * User password stored in plain text
	 */
	public void setPassword(String pass);
	public String getPassword();
	
	/**
	 * User city name
	 */
	public void setCity(String city);
	public String getCity();
	
	/**
	 * User time zone
	 */
	public void setTimeZone(String timeZone);
	public String getTimeZoneName();
	public TimeZone getTimeZone();
}
