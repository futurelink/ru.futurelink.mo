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
import java.util.HashMap;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventConstants;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @since 1.2
 */
@SuppressWarnings("rawtypes")
public class UseCaseActivator implements BundleActivator {	
	private ServiceTracker 			mServiceTracker;
	private Logger					mLogger;
	private UseCaseRegister			mUsecaseRegister;
	
	private HashMap<String, UseCaseInfo>	mUsecaseList;
	
	private UseCaseActivationThread		mActivationThread;
	
	public UseCaseActivator() {
		mLogger = LoggerFactory.getLogger(this.getClass());
		mUsecaseList = new HashMap<String, UseCaseInfo>(); 
	}

	public void addUsecase(UseCaseInfo info) {
		mUsecaseList.put(info.getBundleName(), info);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void start(BundleContext context) throws Exception {
	    mServiceTracker = new ServiceTracker(context, UseCaseRegister.class.getName(), null);
	    mServiceTracker.open();

	    // Run background usecase registration
	    mActivationThread = new UseCaseActivationThread();
	    mActivationThread.start();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// Stop registration requests if there is any
		if ((mActivationThread != null) && (mActivationThread.isAlive())) {
			mActivationThread.interrupt();
		}
		
		if (mUsecaseRegister != null) {
			for (String mUsecaseName : mUsecaseList.keySet()) {
				mLogger.info("Sending '{}' deactivation event to UseCaseRegister...", mUsecaseName);		
				mUsecaseRegister.unregisterUsecase(mUsecaseName);
			}
			mServiceTracker.close();
		} else {
			mLogger.warn("Can't unregister usecase because usecase register is unavailable!");
		}
	}
	
	protected Dictionary<String, Object> getHandlerServiceProperties(String... topics) {
		Dictionary<String, Object> result = new Hashtable<String, Object>();
		result.put(EventConstants.EVENT_TOPIC, topics);
		return result;
	}	
	
	class UseCaseActivationThread extends Thread {		
		public UseCaseActivationThread() {}
		
		@Override
		public void run() {
			// Ждем пока наш сервис регистрации не станет вновь доступным
			do {
				mUsecaseRegister = (UseCaseRegister) mServiceTracker.getService();
				try { Thread.sleep(3000); } catch (InterruptedException ex) {}
				mLogger.info("Usecase register is not available at moment, retrying in 3 secs...");
			} while(mUsecaseRegister == null);

	    	// Отправляем сообщения об активации всех юзкейсов этого бандла
	    	for (String mUsecaseName : mUsecaseList.keySet()) {
	    		mLogger.info("Registering '{}' in UseCaseRegister...", mUsecaseName);
	    		mUsecaseRegister.registerUsecase(mUsecaseList.get(mUsecaseName));
	    	}
		}
	}
}
