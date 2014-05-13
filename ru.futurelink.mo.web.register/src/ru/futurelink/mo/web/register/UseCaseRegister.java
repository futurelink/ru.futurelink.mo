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

	public void registerUsecase(String usecase, Class <?> controller, 
			BundleContext bundleContext) {
		
		if ((usecase != null) && (controller != null)) {
			mRegister.put(usecase, new UseCaseInfo(usecase, controller, bundleContext));
			mLogger.debug("Regiseted usecase with bundle context "+bundleContext.toString());
			mLogger.info("Registered usecase {} with controller {}", usecase, controller.getName());
		} else {
			mLogger.error("Invalid usecase registration information");
		}
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
}
