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

/**
 * @author pavlov
 *
 */
public interface IUserAccess extends ICommonObject {
	public void setGrantedUser(IUser user);
	public IUser getGrantedUser();
	
	public String getEmail();
	public void setEmail(String email);

	public String getLogin();
	public String getUserName();

}
