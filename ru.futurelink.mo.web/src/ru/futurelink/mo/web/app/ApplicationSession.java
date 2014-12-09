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

package ru.futurelink.mo.web.app;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.rap.rwt.RWT;

import ru.futurelink.mo.orm.exceptions.SaveException;
import ru.futurelink.mo.orm.iface.IUser;
import ru.futurelink.mo.orm.pm.PersistentManager;
import ru.futurelink.mo.orm.pm.PersistentManagerSession;
import ru.futurelink.mo.orm.pm.PersistentManagerSessionUI;
import ru.futurelink.mo.orm.security.IUserParams;
import ru.futurelink.mo.orm.security.IUserParamsAccessor;

/**
 * HttpSession wrapper for MO user application sessions.
 * 
 * @author pavlov
 *
 */
final public class ApplicationSession {
	private PersistentManagerSessionUI 	mPersistentSession;

	private String				mLogin = "";
	private Locale				mLocale = null;
	private IUser				mDatabaseUser;
	private Logger				mLogger;
	private BundleContext		mBundleContext;
	private Boolean				mMobileMode = false;
	private IUserParamsAccessor userParamsAccessor;

	public ApplicationSession(BundleContext context) {
		mBundleContext = context;
		mLocale = new Locale("ru", "RU");
		
		// Get persistent manager service
		PersistentManager persistent = (PersistentManager) mBundleContext.getService(
				mBundleContext.getServiceReference(PersistentManager.class.getName())
		);

		// Get user params accessor service
		userParamsAccessor = (IUserParamsAccessor) mBundleContext.getService(
				mBundleContext.getServiceReference(IUserParamsAccessor.class.getName())
		);

		mPersistentSession = new PersistentManagerSessionUI(persistent);

		// Make session never ending
		RWT.getUISession().getHttpSession().setMaxInactiveInterval(8640000);
		
		/*
		 * Get logged in user object from HTTP session.
		 */
		if (RWT.getUISession().getHttpSession().getAttribute("user") != null) {
			// Если у нас есть пользователь, залогиненый в сессии, его
			// надо передать персистент-менеджеру.
			mPersistentSession.setUser((IUser)RWT.getUISession().getHttpSession().getAttribute("user"));

			if (RWT.getUISession().getHttpSession().getAttribute("login") != null)
				mLogin = (String) RWT.getUISession().getHttpSession().getAttribute("login");
		}

        // Create logger available with logger() method.
		mLogger = LoggerFactory.getLogger(ApplicationSession.class);
		
		logger().debug("User locale is {}", RWT.getLocale().toString());
		if ((RWT.getLocale() != null) && (mLocale == null))
			mLocale = RWT.getLocale(); 
	}

    /**
     * Log in as user with specified login.
     *
     * TODO Refactor it. Don't know whether login is needed here.
     *
     * @param user
     * @param login
     * @throws SaveException
     */
	final public void login(IUser user, String login) throws SaveException {
		mLogin = login;
		mPersistentSession.setUser(user);

		RWT.getUISession().getHttpSession().setAttribute("login", mLogin);
		RWT.getUISession().getHttpSession().setAttribute("user", user);

		/*Calendar c = Calendar.getInstance();		
		LoginEventObject leo = new LoginEventObject(mMongoDB);
		leo.setLogin(mLogin);
		leo.setTime(c.getTime());
		leo.save();
        leo.saveCommit();*/

		logger().debug("User {} began session", mLogin);		
	}

	/**
	 * Get session @see Logger object.
	 * 
	 * @return
	 */
	final public Logger logger() {
		return mLogger;
	}
	
	/**
	 * Set session user. Valid @see User object means user is logging in, and
	 * null means user is to be logged out.
	 * 
	 * @param user
	 */
	final public void setUser(IUser user) {
		RWT.getUISession().getHttpSession().setAttribute("user", user);
		
		// Log user enter events to debug session lificycle
		if (user == null) 
			logger().info("Session user is set to null, this means log out.");
		else
			logger().info("Session user is set to object, this means log in.");
	}

	/**
	 * Get session @see User object.
	 * 
	 * @return current session user or null if no user logged in
	 */
	final public IUser getUser() {
		return (IUser) RWT.getUISession().getHttpSession().getAttribute("user");
	}

    /**
     * Set access user for common access to database.
     *
     * @param user
     */
	final public void setDatabaseUser(IUser user) {
		mDatabaseUser = user;
		mPersistentSession.setAccessUser(user);
		logger().debug("Application access user set to: "+user.getUserName());
	}

    /**
     * Get user which has common access to database.
     *
     * @return
     */
	final public IUser getDatabaseUser() {
		if (mDatabaseUser != null) {
			return mDatabaseUser;
		} else {
			return (IUser) RWT.getUISession().getHttpSession().getAttribute("user");
		}
	}
	
	/**
	 * <p>Get persistent manager session.</p>
	 * 
	 * <p>The persistent manager session is used to create entity managers, manipulate
	 * object via EclipseLink ORM. Other words it's a wrapper for persistence provider
	 * created by Gemini DBA and EclipseLink.</p>
	 * 
	 * @see PersistentManagerSession
	 * 
	 * @return
	 */
	final public PersistentManagerSessionUI persistent() {
		// If user is released by some reason, try to reset it from session variable
		if (mPersistentSession.getUser() == null) {
			logger().warn("Persistent manager has null user. Trying to reset from session.");
			mPersistentSession.setUser(
				(IUser) RWT.getUISession().getHttpSession().getAttribute("user")
			);
		}
		return mPersistentSession;
	}

    /**
     * Get mongoDb provider.
     *
     * TODO refactor it - remove direct mongodb access.
     *
     * @return
     */
	/*final public MongoDBProvider mongo() {
		return mMongoDB;
	}*/
	
	/**
	 * Get if user is logged in.
	 * 
	 * @return
	 */
	final public Boolean isLoggedIn() {
		return getUser() != null;
	}

	/**
	 * Get HttpSession ID.
	 * 
	 * @return
	 */
	final public String getId() {		
		return RWT.getUISession().getHttpSession().getId();
	}

	/**
	 * <p>Get user locale object.</p>
	 * 
	 * <p>The locale object may be set by user or is detected from the
	 * browser locale.</p>
	 * 
	 * @return
	 */
	final public Locale getLocale() {
		return mLocale;
	}

	final public void setLanguage(String language) {
		if (language.toUpperCase().equals("RUSSIAN") || language.toUpperCase().equals("RU")) {
			mLocale = new Locale("ru", "RU");
		} else {
			mLocale = new Locale("en", "EN");
		}
	}

	/**
	 * Terminate user session.
	 */
	final public void logout() {
		logger().debug("User {} session ended", mLogin);
		
		mLogin = null;
		mPersistentSession.setUser(null);

		RWT.getUISession().getHttpSession().setAttribute("login", mLogin);
		RWT.getUISession().getHttpSession().setAttribute("user", null);
	}
	
	/**
	 * Set session mobile mode.
	 * 
	 * @param isMobile
	 */
	public void setMobileMode(boolean isMobile) {
		mMobileMode = isMobile;
	}
	
	/**
	 * Get session is in mobile mode.
	 * 
	 * @return
	 */
	public boolean getMobileMode() {
		return mMobileMode;
	}
	
	/**
     * Get user parameters. When user changes some UI properties or values they can be stored
     * in user parameters structure in user session and profile and then loaded when they are needed.
     *
	 * @return
	 */
	public IUserParams getUserParams(String usecaseName, String paramName) {
		if (userParamsAccessor != null) {
			return userParamsAccessor.getUserParams(getUser().getId(), usecaseName, paramName);
		} else {
			return null;
		}
	}

    /**
     * Save user parameters.
     *
     * @param params
     * @throws SaveException
     */
	public void setUserParams(IUserParams params) throws SaveException {
		if (userParamsAccessor != null) {
			userParamsAccessor.saveUserParams(params);
		}
	}

    /**
     * Translate given date and time into user's time zone.
     *
     * @param dateTime
     * @return
     */
	public Date userTime(Date dateTime) {
		Calendar c = Calendar.getInstance();	// In server time
		c.setTimeInMillis(dateTime.getTime() - TimeZone.getDefault().getRawOffset()
				+ getUser().getTimeZone().getRawOffset());
		
		return c.getTime();
	}
}
