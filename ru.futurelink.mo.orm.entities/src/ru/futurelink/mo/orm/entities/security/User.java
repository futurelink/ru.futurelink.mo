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

package ru.futurelink.mo.orm.entities.security;

import java.util.TimeZone;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import ru.futurelink.mo.orm.annotations.Accessors;
import ru.futurelink.mo.orm.entities.CommonObject;
import ru.futurelink.mo.orm.iface.IUser;
import ru.futurelink.mo.orm.pm.IPersistentManagerSession;

@Entity(name = "User")
@Table(name = "USERS")
@NamedQueries({
	@NamedQuery(name="User.login", query="SELECT u FROM User u where u.mLogin = :login and u.mPassword = :password"),
	@NamedQuery(name="User.findUserByLogin", query="SELECT u FROM User u where u.mLogin = :login"),
	@NamedQuery(name="User.findUserByEmail", query="SELECT u FROM User u where u.mEmail = :email")
})
public class User 
	extends CommonObject 
	implements IUser {
	private static final long serialVersionUID = 1L;

	protected User() {}

	public User(IPersistentManagerSession manager) {
		super(manager);
	}

	@Column(name = "email")
	@Accessors(setter = "setEmail", getter = "getEmail")
	private String mEmail;
	public void setEmail(String email) { mEmail = email; }
	public String getEmail() { return mEmail; }
	
	/**
	 * User name
	 */
	@Column(name = "userName")
	@Accessors(setter = "setUserName", getter = "getUserName")
	private String mUserName;
	public void setUserName(String userName) { mUserName = userName; }
	public String getUserName() { return mUserName; }
	
	/**
	 * User login
	 */
	@Column(name = "login")
	@Accessors(setter = "setLogin", getter = "getLogin")
	private String mLogin;
	public void setLogin(String login) { mLogin = login; }
	public String getLogin() { return mLogin; }
	
	/**
	 * User password stored in plain text
	 */
	@Column(name = "password")
	@Accessors(setter = "setPassword", getter = "getPassword")
	private String mPassword;
	public void setPassword(String pass) { mPassword = pass; }
	public String getPassword() { return mPassword; }
	
	/**
	 * User city name
	 */
	@Column(name = "city")
	@Accessors(setter = "setCity", getter = "getCity")
	private String mCity;
	public void setCity(String city) { mCity = city; }
	public String getCity() { return mCity; }
	
	/**
	 * User time zone
	 */
	@Column(name = "timezone")
	@Accessors(setter = "setTimeZone", getter = "getTimeZoneName")
	private String mTimeZone;
	public void setTimeZone(String timeZone) { mTimeZone = timeZone; }
	public String getTimeZoneName() { return mTimeZone; }
	public TimeZone getTimeZone() {
		if (mTimeZone != null) {
			return TimeZone.getTimeZone(mTimeZone);
		} else {
			return TimeZone.getDefault();
		}
	}
	
	public String getGrantedUsersCount() {
		Query q = getPersistenceManagerSession().getEm().createQuery(
				"select count(d) from Access d where d.owner = :owner and d.deleteFlag = 0"
			);
		q.setParameter("owner", this);
		if (q.getResultList().size() > 0) {
			logger().debug("Количество: {}", q.getSingleResult());
			return q.getSingleResult().toString();
		}
		
		return null; 
	}

	public User getSystemUser() {
		TypedQuery<User> q = getPersistenceManagerSession().getEm().createNamedQuery(
				"User.findUserByLogin", User.class
			);
		q.setParameter("login", "futurelink.vl@gmail.com");
		if (q.getResultList().size() > 0) {
			return q.getSingleResult();
		}

		return null;
	}
}
