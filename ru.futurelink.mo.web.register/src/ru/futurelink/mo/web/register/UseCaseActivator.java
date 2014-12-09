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
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventConstants;

/**
 * @since 1.2
 */
public class UseCaseActivator implements BundleActivator {	
	private Logger						mLogger;
	private UseCaseActivationThread		mActivationThread;
	private HashMap<String, UseCaseInfo>	mUsecaseList;
	
	public UseCaseActivator() {
		mLogger = LoggerFactory.getLogger(this.getClass());
		mUsecaseList = new HashMap<String, UseCaseInfo>(); 
	}

	public void addUsecase(UseCaseInfo info) {
		mUsecaseList.put(info.getBundleName(), info);
	}

	@Override
	public void start(BundleContext context) throws Exception {
	    // Run background usecase registration
	    mActivationThread = new UseCaseActivationThread(context);
	    mActivationThread.start();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// Stop registration requests if there is any
		if ((mActivationThread != null) && (mActivationThread.isAlive())) {
			mActivationThread.interrupt();
		}
		
		ServiceReference<?> ref = context.getServiceReference(UseCaseRegister.class);
		UseCaseRegister usecaseRegister = (UseCaseRegister) context.getService(ref);
		if (usecaseRegister != null) {
			for (String mUsecaseName : mUsecaseList.keySet()) {
				mLogger.info("Sending '{}' deactivation event to UseCaseRegister...", mUsecaseName);		
				usecaseRegister.unregisterUsecase(mUsecaseName);
			}
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
		private volatile BundleContext		context;

		public UseCaseActivationThread(BundleContext context) {
			this.context = context;
		}
		
		@Override
		public void run() {
			// Wait while service is unavailable
			UseCaseRegister	 usecaseRegister;
			do {
				ServiceReference<?> ref = context.getServiceReference(UseCaseRegister.class);
				usecaseRegister = (UseCaseRegister) context.getService(ref);
				if (usecaseRegister == null) {
					try { Thread.sleep(3000); } catch (InterruptedException ex) {}
					mLogger.info("Usecase register is not available at moment, retrying in 3 secs...");
				}
			} while(usecaseRegister == null);

	    	// Register all usecases on this bundle
	    	for (String mUsecaseName : mUsecaseList.keySet()) {
	    		mLogger.info("Registering '{}' in UseCaseRegister...", mUsecaseName);
	    		usecaseRegister.registerUsecase(mUsecaseList.get(mUsecaseName));
	    	}
		}
	}
}
