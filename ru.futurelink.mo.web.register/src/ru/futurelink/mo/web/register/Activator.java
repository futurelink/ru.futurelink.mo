package ru.futurelink.mo.web.register;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

/**
 * The activator class controls the plug-in life cycle!
 * @since 1.2
 */
@SuppressWarnings("rawtypes")
public class Activator implements BundleActivator {
	private ServiceRegistration mUsecaseRegistration;
	private ServiceRegistration mEventHandlerRegistration;
	
	private ApplicationConfig config;

	@Override
	public void start(BundleContext context) {
		
		/*
		 *  Зарегистрируем сервис регистра юзкейсов.
		 */
		if (mUsecaseRegistration == null) {
			mUsecaseRegistration = context.registerService(
					UseCaseRegister.class.getName(), 
					 new UseCaseRegister(context), 
					 null
				);
		}
	
		/*
		 *  Зарегистрируем обработчик для подключений точек входа
		 *  к основному бандлу.
		 */		
		config = new ApplicationConfig(context);
		context.registerService(
				EntryPointRegister.class.getName(),
				new EntryPointRegister(config),
				null
			);

		// Устаревщий регистратор
		mEventHandlerRegistration = context.registerService(
				EventHandler.class.getName(),
		        new EntryPointActivationEventHandler(config),
		        getHandlerServiceProperties("ru/futurelink/mo/web/entrypoint/Activator")
			);
	}

	@Override
	public void stop(BundleContext context) {
		config = null;		
		
		if (mEventHandlerRegistration != null)
			mEventHandlerRegistration.unregister();

		try {
			ServiceReference<UseCaseRegister> ref = context.getServiceReference(UseCaseRegister.class);
			if (ref != null) {
				// TODO Send notification to registered use cases to maketheir state
				// as not registered.

				// And remove UseCaseRegister service
				context.ungetService(ref);
			}
		} catch (IllegalStateException ex) {
			
		}
	}

	protected Dictionary<String, Object> getHandlerServiceProperties(String... topics) {
		Dictionary<String, Object> result = new Hashtable<String, Object>();
		result.put(EventConstants.EVENT_TOPIC, topics);
		return result;
	}

}