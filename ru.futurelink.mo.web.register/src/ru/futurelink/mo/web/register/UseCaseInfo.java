package ru.futurelink.mo.web.register;

import org.osgi.framework.BundleContext;

/**
 * @since 1.2
 */
public class UseCaseInfo {	
	private String	 							mUseCaseBundle;
	private Class <?>							mUseCaseController;
	private String								mNavigationTag;

	private BundleContext						mBundleContext;

	public UseCaseInfo(String usecase, Class <?> controller, 
			BundleContext context) {
		mUseCaseBundle = usecase;
		mUseCaseController = controller;
		mBundleContext = context;
	}

	public UseCaseInfo(String usecase, Class <?> controller, 
			String navigationTag, BundleContext context) {
		mUseCaseBundle = usecase;
		mUseCaseController = controller;
		mBundleContext = context;
		mNavigationTag = navigationTag;
	}
	
	public String getNavigationTag() {
		return mNavigationTag;
	}
	
	public Class <?> getControllerClass() {
		return mUseCaseController;
	}

	public String getBundleName() {
		return mUseCaseBundle;
	}
	
	public BundleContext getBundleContext() {
		return mBundleContext;
	}
}
