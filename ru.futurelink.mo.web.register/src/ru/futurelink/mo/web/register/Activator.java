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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * The activator class controls the plug-in life cycle!
 * @since 1.2
 */
@SuppressWarnings("rawtypes")
public class Activator implements BundleActivator {
	private ServiceRegistration<?> mUsecaseRegistration;
	private ServiceRegistration<?> mEntryPointRegistration;
	private ServiceRegistration mEventHandlerRegistration;
	
	@Override
	public synchronized void start(BundleContext context) {
		
		/*
		 *  Зарегистрируем сервис регистра юзкейсов.
		 */
		if (mUsecaseRegistration == null) {
			mUsecaseRegistration = context.registerService(
					UseCaseRegister.class.getName(), 
					UseCaseRegister.getInstance(), 
					null
				);
			Dictionary<String, Object> dic = new Hashtable<String, Object>();
			dic.put("info", "Usecase registry service");
			mUsecaseRegistration.setProperties(dic);			
		}
	
		/*
		 *  Зарегистрируем обработчик для подключений точек входа
		 *  к основному бандлу.
		 */		
		if (mEntryPointRegistration == null) {
			mEntryPointRegistration = context.registerService(
					EntryPointRegister.class.getName(),
					EntryPointRegister.getInstance(),
					null
				);
			Dictionary<String, Object> dic = new Hashtable<String, Object>();
			dic.put("info", "EntryPoint registry service");
			mEntryPointRegistration.setProperties(dic);
		}
	}

	@Override
	public void stop(BundleContext context) {		
		if (mEventHandlerRegistration != null)
			mEventHandlerRegistration.unregister();

		try {
			ServiceReference<UseCaseRegister> ref = context.getServiceReference(UseCaseRegister.class);
			if (ref != null) {
				// TODO Send notification to registered use cases to maketheir state
				// as not registered.

				// And remove UseCaseRegister service
				context.ungetService(ref);
				
				ref = null;
			}
		} catch (IllegalStateException ex) {
			
		}

		try {			
			ServiceReference<EntryPointRegister> ref = context.getServiceReference(EntryPointRegister.class);
			if (ref != null) {
				// Stop applications
				EntryPointRegister register = context.getService(ref);
				register.stop();

				// Remove EntryPointRegister service				
				context.ungetService(ref);
				
				register = null;
				ref = null;
			}
		} catch (IllegalStateException ex) {
			
		}	
	}
}