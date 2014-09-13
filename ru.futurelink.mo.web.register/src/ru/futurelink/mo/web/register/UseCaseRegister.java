package ru.futurelink.mo.web.register;

import java.util.HashMap;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 1.2
 */
public class UseCaseRegister {
	private HashMap<String, UseCaseInfo>	mRegister;
	private Logger							mLogger;
	private	 BundleContext					mMainBundleContext;
	
	public UseCaseRegister(BundleContext context) {
		mRegister = new HashMap<String, UseCaseInfo>();
		mLogger = LoggerFactory.getLogger(this.getClass());
		mMainBundleContext = context;
	}

	public void registerUsecase(UseCaseInfo info) {
			mRegister.put(info.getBundleName(), info);
			mLogger.debug("Regiseted usecase with bundle context {}", 
					info.getBundleContext().toString()
				);
			mLogger.info("Registered usecase {} with controller {}", 
					info.getBundleName(),
					info.getControllerClass().getName()
				);
	}
	
	public void registerUsecase(String usecase, Class <?> controller, 
			Class <?> dataClass, BundleContext bundleContext) {
		registerUsecase(usecase, controller, dataClass, null, bundleContext);
	}
			
	public void registerUsecase(String usecase, Class <?> controller, 
			Class<?> dataClass, String navigationTag, BundleContext bundleContext) {
		if ((usecase != null) && (controller != null)) {
			UseCaseInfo info = new UseCaseInfo(
					usecase, controller, dataClass, navigationTag,  bundleContext
				);
			registerUsecase(info);
		} else {
			mLogger.error("Invalid usecase registration information");
		}
	}

	public String getUsecaseByNavigationTag(String tag) {
		if (tag == null) return null;

		for (String usecase : mRegister.keySet()) {
			if (tag.equals(mRegister.get(usecase).getNavigationTag())) {
				return  mRegister.get(usecase).getBundleName();
			}
		}
		
		return null;
	}
	
	public void unregisterUsecase(String usecase) {
		if (mRegister.containsKey(usecase)) {
			mRegister.remove(usecase);
			mLogger.info("Unregistered usecase {}", usecase);
		}
	}
	
	public Class <?> getController(String usecase) {
		if (mRegister.containsKey(usecase)) {
			return mRegister.get(usecase).getControllerClass();
		} else {
			mLogger.error("Usecase {} is not registered", usecase);
			return null;
		}
	}
	
	public String getBundleName(Class <?> clazz) {
		for (String k : mRegister.keySet())
			if (mRegister.get(k).getControllerClass().equals(clazz))
				return k;
		return null;
	}
	
	public BundleContext getMainBundleContext() {
		return mMainBundleContext;
	}
	
	public BundleContext getBundleContext(String usecase) {
		if (mRegister.containsKey(usecase)) {
			return mRegister.get(usecase).getBundleContext();
		} else {
			mLogger.error("Usecase {} is not registered", usecase);
			return null;
		}
	}

	/**
	 * @param usecaseBundle
	 * @return
	 */
	public UseCaseInfo getInfo(String usecaseBundle) {
		return mRegister.get(usecaseBundle);
	}
}
