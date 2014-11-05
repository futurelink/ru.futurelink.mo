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

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.application.EntryPointFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import ru.futurelink.mo.web.register.ApplicationConfig;

/**
 * Обработчик запросов от бандлов точек входа на регистрацию и очистку
 * точек доступа приложения.
 * 
 * @author pavlov
 *
 */
public class EntryPointActivationEventHandler implements EventHandler {

	private ApplicationConfig 	mApplicationConfig;
	
	public EntryPointActivationEventHandler(
			ApplicationConfig applicationConfig) {
		mApplicationConfig = applicationConfig;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void handleEvent(Event event) {
		String title = (String) event.getProperty("title");
		String favicon = (String) event.getProperty("favicon");
		String url = (String) event.getProperty("url");
		String mode = (String) event.getProperty("mode");

		EntryPointFactory factory = 
				(EntryPointFactory) event.getProperty("entryPoint");

		if ((url == null) || (mode == null)) {
			System.out.println("Пришло какое-то корявое сообщение о регистрации точки входа: "+title);
			return;
		}

		// Выключаем сервис и включаем заново, чтобы обработать все точки входа
		BundleContext mBundleContext = FrameworkUtil.getBundle(ApplicationConfiguration.class).getBundleContext();
		ServiceReference<ApplicationConfiguration> ref = mBundleContext.getServiceReference(ApplicationConfiguration.class);
		synchronized (this) {
			if (ref != null) {
				mApplicationConfig.getRegistration().unregister();
				System.out.println("Сервис RAP отключен");
			}
				
			if (mode.equals("active")) {
				if (factory != null) {
					mApplicationConfig.addEntryPoint(title, url, favicon, 
						factory);
					System.out.println("Точка входа "+title+" на "+url+" зарегистрирована.");
				} else {
					System.out.println("Точка входа на "+url+" не может быть зарегистрирована, нет объекта самой точки входа.");
				}
			} else if(mode.equals("inactive")) {
				mApplicationConfig.removeEntryPoint(url);
				System.out.println("Точка входа на "+url+" удалена.");					
			} else {
				System.out.println("Режим "+mode+" может быть только 'active' или 'inactive'. Ничего не изменилось.");
			}			

			// Регистрируем приложение обратно
			Dictionary<String, Object> props = new Hashtable<String, Object>();
			props.put("contextName", "mo");

			mApplicationConfig.setRegistration(
				mBundleContext.registerService(ApplicationConfiguration.class.getName(), 
						mApplicationConfig, 
						props));
		
			System.out.println("Сервис RAP включен");
		}

		//System.out.println("Сервис RAP не может быть перезагружен, новая точка входа не доступна.");
			
	}

}
