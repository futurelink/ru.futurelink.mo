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

package ru.futurelink.mo.web.register;

import java.io.InputStream;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.application.EntryPointFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.log.LogService;

/**
 * Регистр точек входа в приложение RWT. Используется для переконфигурирования
 * прилоежния на лету.
 * 
 * В процессе переконфигурации сервис RWT ApplicationConfiguration будет остановлен, 
 * переконфигурирован и запущен вновь. 
 * 
 * @author pavlov
 *
 */
public class EntryPointRegister {
	private LogService				mLogService;
	private BundleContext			mContext;
	private ApplicationConfig 		mApplicationConfig;
	private String					mApplicationContext;
	
	private static EntryPointRegister Instance = new EntryPointRegister(
		new ApplicationConfig(
			FrameworkUtil.getBundle(ApplicationConfiguration.class).getBundleContext()
		)
	); 
	
	public static EntryPointRegister getInstance() {
		return Instance;
	}
	
	private EntryPointRegister(ApplicationConfig applicationConfig) {
		mApplicationConfig = applicationConfig;	
		mApplicationContext = "";
	}
	
	public void setApplicationContext(String appContext) {
		mApplicationContext = appContext;
		unregisterAppConfig();
		registerAppConfig();
		
		mLogService.log(LogService.LOG_INFO, "Application context changed to '"+mApplicationContext+"'" , null);
	}

	public void stop() {
		preInit();
		unregisterAppConfig();
		mApplicationConfig.removeAllEntryPoints();
	}

	private void preInit() {
		mContext = FrameworkUtil.getBundle(ApplicationConfiguration.class).getBundleContext();
		
		/*
		 * Get logging service instance
		 */
		mLogService = (LogService) mContext.getService(
				mContext.getServiceReference(LogService.class.getName())
			);
	}
	
	private void unregisterAppConfig() {
		if ((mApplicationConfig != null) && (mApplicationConfig.getRegistration() != null)) {
			mApplicationConfig.getRegistration().unregister();
			mLogService.log(LogService.LOG_INFO, "RWT application service stopped", null);
		}			
	}

	private void registerAppConfig() {
		// Register application service back again
		Dictionary<String, Object> props = new Hashtable<String, Object>();
		if ((mApplicationContext != null) && !("".equals(mApplicationContext))) {
			props.put("contextName", mApplicationContext);
		}
		
		mApplicationConfig.setRegistration(
				mContext.registerService(ApplicationConfiguration.class.getName(), 
						mApplicationConfig, 
						props
					)
			);
		
		mLogService.log(LogService.LOG_INFO, "RWT application service started", null);
	}
	
	/**
	 * Register application entry point in configuration.
	 * 
	 * @param title
	 * @param url
	 * @param favicon
	 * @param factory
	 */
	public void addEntryPoint(String title, String url, InputStream favicon, EntryPointFactory factory) {
		preInit();
		unregisterAppConfig();
		mApplicationConfig.addEntryPoint(
				title, 
				url, 
				favicon, 
				factory);
		registerAppConfig();
	}
	
	/**
	 * Remove application entry point from configuration. 
	 * 
	 * @param url
	 */
	public void removeEntryPoint(String url) {
		preInit();
		unregisterAppConfig();		
		mApplicationConfig.removeEntryPoint(url);		
		registerAppConfig();
	}
}
