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

import org.eclipse.rap.rwt.application.EntryPoint;
import org.eclipse.rap.rwt.application.EntryPointFactory;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;

import ru.futurelink.mo.web.register.UseCaseRegister;

@SuppressWarnings("rawtypes")
abstract public class ApplicationEntryPointFactory implements EntryPointFactory {

	private ServiceTracker mServiceTracker;
	private UseCaseRegister mUsecaseRegister;
	private BundleContext mContext;
	private Logger			mLogger;
	
	@SuppressWarnings("unchecked")
	public ApplicationEntryPointFactory(BundleContext context) {
		mContext = context;

	    mServiceTracker = new ServiceTracker(
	    		context, 
	    		UseCaseRegister.class.getName(), 
	    		null);
	    mServiceTracker.open();
	    
	    try {
	    	mUsecaseRegister = (UseCaseRegister) mServiceTracker.waitForService(5000);
	    } catch (InterruptedException ex) {
	    	mLogger.error("No usecase register available in 5 seconds!");
	    }
	}

	protected UseCaseRegister getUsecaseRegister() {
		return mUsecaseRegister;
	}

	protected BundleContext getBundleContext() {
		return mContext;
	}

	public EntryPoint create() {
		return null;
	}
}
