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

import java.util.TimeZone;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.orm.PersistentManagerSession;

@Entity(name = "User")
@Table(name = "USERS")
@NamedQueries({
	@NamedQuery(name="User.login", query="SELECT u FROM User u where u.mLogin = :login and u.mPassword = :password"),
	@NamedQuery(name="User.findUserByLogin", query="SELECT u FROM User u where u.mLogin = :login"),
	@NamedQuery(name="User.findUserByEmail", query="SELECT u FROM User u where u.mEmail = :email")
})
public class User extends CommonObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected User() {}

	public User(PersistentManagerSession manager) {
		super(manager);
	}

	@Column(name = "email")
	private String mEmail;
	public void setEmail(String email) { mEmail = email; }
	public String getEmail() { return mEmail; }
	
	/**
	 * Имя пользователя
	 */
	@Column(name = "userName")
	private String mUserName;
	public void setUserName(String userName) { mUserName = userName; }
	public String getUserName() { return mUserName; }
	
	/**
	 * Логин пользователя
	 */
	@Column(name = "login")
	private String mLogin;
	public void setLogin(String login) { mLogin = login; }
	public String getLogin() { return mLogin; }
	
	/**
	 * Пароль пользователя
	 */
	@Column(name = "password")
	private String mPassword;
	public void setPassword(String pass) { mPassword = pass; }
	public String getPassword() { return mPassword; }
	
	/**
	 * Город пользователя
	 */
	@Column(name = "city")
	private String mCity;
	public void setCity(String city) { mCity = city; }
	public String getCity() { return mCity; }
	
	/**
	 * Часовой пояс пользователя
	 */
	@Column(name = "timezone")
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
		Query q = getPersistenceManagerSession().getEm().createQuery("select count(d) from Access d where d.mCreator = :creator and d.mDeleteFlag = 0");
		q.setParameter("creator", this);
		if (q.getResultList().size() > 0) {
			logger().debug("Количество: {}", q.getSingleResult());
			return q.getSingleResult().toString();
		}
		
		return null; 
	}

	public User getSystemUser() {
		TypedQuery<User> q = getPersistenceManagerSession().getEm().createNamedQuery("User.findUserByLogin", User.class);
		q.setParameter("login", "futurelink.vl@gmail.com");
		if (q.getResultList().size() > 0) {
			return q.getSingleResult();
		}

		return null;
	}
}
