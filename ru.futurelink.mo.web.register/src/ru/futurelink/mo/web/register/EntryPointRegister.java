/**
 * 
 */
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

	protected EntryPointRegister(ApplicationConfig applicationConfig) {
		mApplicationConfig = applicationConfig;	
		mApplicationContext = "";
	}
	
	public void setApplicationContext(String appContext) {
		mApplicationContext = appContext;
		unregisterAppConfig();
		registerAppConfig();
		
		mLogService.log(LogService.LOG_INFO, "Контекст приложения изменился на "+mApplicationContext , null);
	}

	private void preInit() {
		mContext = FrameworkUtil.getBundle(ApplicationConfiguration.class).getBundleContext();
		
		/*
		 * Получаем доступ к сервису логирования
		 */
		mLogService = (LogService) mContext.getService(
				mContext.getServiceReference(LogService.class.getName())
			);
	}
	
	private void unregisterAppConfig() {
		if ((mApplicationConfig != null) && (mApplicationConfig.getRegistration() != null)) {
			mApplicationConfig.getRegistration().unregister();
			mLogService.log(LogService.LOG_INFO, "Сервис приложения остановлен", null);
		}			
	}

	private void registerAppConfig() {
		// Регистрируем приложение обратно
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
		
		mLogService.log(LogService.LOG_INFO, "Сервис приложения запущен", null);
	}
	
	/**
	 * Зарегистрировать точку входа в приложение в конфигурации.
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
	 * Удалить точку входа в приложение из конфигурации. 
	 * @param url
	 */
	public void removeEntryPoint(String url) {
		preInit();
		unregisterAppConfig();		
		mApplicationConfig.removeEntryPoint(url);		
		registerAppConfig();
	}
}
