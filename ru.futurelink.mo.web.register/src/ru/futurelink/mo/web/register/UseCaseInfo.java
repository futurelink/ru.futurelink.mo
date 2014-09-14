package ru.futurelink.mo.web.register;

import org.osgi.framework.BundleContext;

/**
 * @since 1.2
 */
public class UseCaseInfo {	
	private String	 							mUseCaseBundle;
	private Class <?>							mUseCaseController;
	private String								mNavigationTag;
	private Class<?>							mDataClass;

	private BundleContext						mBundleContext;

	public UseCaseInfo(String usecase, Class <?> controller, 
			Class<?> dataClass, BundleContext context) {
		mUseCaseBundle = usecase;
		mUseCaseController = controller;
		mBundleContext = context;
		mDataClass = dataClass;
	}

	public UseCaseInfo(String usecase, Class <?> controller, Class<?> dataClass, 
			String navigationTag, BundleContext context) {
		mUseCaseBundle = usecase;
		mUseCaseController = controller;
		mBundleContext = context;
		mNavigationTag = navigationTag;
		mDataClass = dataClass;
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
	
	public Class <?> getDataClass() {
		return mDataClass;
	}
}
