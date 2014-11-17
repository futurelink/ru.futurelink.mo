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

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.JoinColumn;
import javax.persistence.Transient;

import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.orm.pm.PersistentManagerSession;

@Entity(name = "Access")
@Table(name = "ACCESS")
@NamedQueries({
	@NamedQuery(name="Access.all", query="SELECT d FROM Access d where d.mCreator = :creator and d.deleteFlag = 0"),
})
public class Access extends CommonObject {
	private static final long serialVersionUID = 1L;

	protected Access() {

	}

	public Access(PersistentManagerSession manager) {
		super(manager);
	}
	
	@JoinColumn(name = "GRANTED_USER", referencedColumnName="id")
	private User mGrantedUser;
	public void setGrantedUser(User user) { mGrantedUser = user; }
	public User getGrantedUser() { return mGrantedUser; }
	
	@Transient
	private String mEmail;
	public String getEmail() {
		if (mGrantedUser != null) return  mGrantedUser.getEmail(); else return mEmail;
	}
	public void setEmail(String email) {
	    mEmail = email;
	}

	public String getLogin() { 
		if (mGrantedUser != null) return mGrantedUser.getLogin(); else return null; 
	}

	public String getUserName() { 
		if (mGrantedUser != null) return mGrantedUser.getUserName(); else return null;
	}
}
