package ru.futurelink.mo.web.app;

import java.util.Calendar;
import java.util.Locale;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.rap.rwt.RWT;

import ru.futurelink.mo.orm.PersistentManager;
import ru.futurelink.mo.orm.PersistentManagerSession;
import ru.futurelink.mo.orm.mongodb.MongoDBProvider;
import ru.futurelink.mo.orm.mongodb.objects.LoginEventObject;
import ru.futurelink.mo.orm.mongodb.objects.UserParams;
import ru.futurelink.mo.orm.security.User;

/**
 * Объект оболочка для сессии приложения над HttpSession.
 * @author Futurelink
 * @since 1.2
 *
 */
final public class ApplicationSession {
	private PersistentManagerSession 	mPersistentSession;

	private MongoDBProvider		mMongoDB;
	private String				mLogin = "";
	private Locale				mLocale = null;
	private User				mDatabaseUser;
	private Logger				mLogger;
	private BundleContext		mBundleContext;
	private Boolean				mMobileMode = false;

	public ApplicationSession(BundleContext context) {
		mBundleContext = context;
		mLocale = new Locale("ru", "RU");
		PersistentManager persistent = (PersistentManager) mBundleContext.getService(
				mBundleContext.getServiceReference(PersistentManager.class.getName())
		);

		mPersistentSession = new PersistentManagerSession(persistent);

		// Make session never ending
		RWT.getUISession().getHttpSession().setMaxInactiveInterval(8640000);

		// Создаем подключение к MongoDB, база "fluvio"
		mMongoDB = new MongoDBProvider("fluvio");
		
		/*
		 * Восстанавливаем данные из сессии в объект.
		 */
		if (RWT.getUISession().getHttpSession().getAttribute("user") != null) {
			// Если у нас есть пользователь, залогиненый в сессии, его
			// надо передать персистент-менеджеру.
			mPersistentSession.setUser((User)RWT.getUISession().getHttpSession().getAttribute("user"));

			if (RWT.getUISession().getHttpSession().getAttribute("login") != null)
				mLogin = (String) RWT.getUISession().getHttpSession().getAttribute("login");
		}

		mLogger = LoggerFactory.getLogger(ApplicationSession.class);
		
		logger().debug("Локаль пользователя {}", RWT.getLocale().toString());
		if ((RWT.getLocale() != null) && (mLocale == null))
			mLocale = RWT.getLocale(); 
	}

	final public void login(User user, String login) {
		mLogin = login;
		mPersistentSession.setUser(user);

		RWT.getUISession().getHttpSession().setAttribute("login", mLogin);
		RWT.getUISession().getHttpSession().setAttribute("user", user);

		// Сохраняем данные о входе пользователя
		Calendar c = Calendar.getInstance();		
		LoginEventObject leo = new LoginEventObject(mMongoDB);
		leo.setLogin(mLogin);
		leo.setTime(c.getTime());
		leo.save();

		logger().debug("Начата сессия пользователя {}", mLogin);		
	}

	final public Logger logger() {
		return mLogger;
	}
	
	final public void setUser(User user) {
		RWT.getUISession().getHttpSession().setAttribute("user", user);
		
		// Log user enter events to debug session lificycle
		if (user == null) 
			logger().info("Session user is set to null, this means log out.");
		else
			logger().info("Session user is set to object, this means log in.");
	}

	final public User getUser() {
		return (User) RWT.getUISession().getHttpSession().getAttribute("user");
	}

	final public void setDatabaseUser(User user) {
		mDatabaseUser = user;
		mPersistentSession.setAccessUser(user);
		logger().debug("Application access user set to: "+user.getUserName());
	}
	
	final public User getDatabaseUser() {
		if (mDatabaseUser != null) {
			return mDatabaseUser;
		} else {
			return (User) RWT.getUISession().getHttpSession().getAttribute("user");
		}
	}
	
	final public PersistentManagerSession persistent() {
		// If user is released by some reason, try to reset it from session variable
		if (mPersistentSession.getUser() == null) {
			logger().warn("Persistent manager has null user. Trying to reset from session.");
			mPersistentSession.setUser(
				(User) RWT.getUISession().getHttpSession().getAttribute("user")
			);
		}
		return mPersistentSession;
	}

	final public MongoDBProvider mongo() {
		return mMongoDB;
	}
	
	final public Boolean isLoggedIn() {
		return getUser() != null;
	}

	final public String getId() {		
		return RWT.getUISession().getHttpSession().getId();
	}

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
	 * 
	 */
	final public void logout() {
		logger().debug("Завершена сессия пользователя {}", mLogin);
		
		mLogin = null;
		mPersistentSession.setUser(null);

		RWT.getUISession().getHttpSession().setAttribute("login", mLogin);
		RWT.getUISession().getHttpSession().setAttribute("user", null);
	}
	
	public void setMobileMode(boolean isMobile) {
		mMobileMode = isMobile;
	}
	
	public boolean getMobileMode() {
		return mMobileMode;
	}
	
	/**
	 * Получить параметры пользователя. Или пользовательские параметры - кому как больше нравится.
	 * Суть в том, что это пользователь может менят какие-то параметры интерфейса, а они будут
	 * сохраняться в сессии, а потом и в профиле пользователя. 
	 * 
	 * @return
	 */
	public UserParams getUserParams(String usecaseName, String paramName) {		
		// Формируем запрос параметров
		UserParams paramsQuery = new UserParams(mMongoDB);
		paramsQuery.setUser(getUser().getId());
		paramsQuery.setUsecaseName(usecaseName);
		paramsQuery.setParamName(paramName);
		
		// Выполняем запрос параметров
		paramsQuery.queryAndFill();
		
		return paramsQuery;
	}
	
	public void setUserParams(UserParams params) {
		params.save();
	}

}
