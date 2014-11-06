package ru.futurelink.mo.demo.usecase;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.futurelink.mo.demo.app.DemoApplicationEntryPointFactory;
import ru.futurelink.mo.web.controller.CommonController;
import ru.futurelink.mo.web.register.EntryPointRegister;

public class Activator implements BundleActivator {

	private static String favicon = "/images/16/document_time.png";
	
	private Logger logger;
	
	public Activator() {}

	@Override
	public void start(BundleContext context) throws Exception {
		logger = LoggerFactory.getLogger(getClass());

		EntryPointRegister register = (EntryPointRegister) context.getService(
				context.getServiceReference(EntryPointRegister.class.getName())
			);

		if (register != null) {
			register.addEntryPoint(
					"Demo application", 
					"/demo", 
					CommonController.class.getResourceAsStream(favicon),
					 new DemoApplicationEntryPointFactory(context)
				);
			
			logger.info("Started demo application entry point");
		} else {			
			String exString ="Can't register entry point because of EntryPointRegsiter is unavailable!"; 
			logger.error(exString);
			throw new RuntimeException(exString);
		}	
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		EntryPointRegister register = (EntryPointRegister) context.getService(
				context.getServiceReference(EntryPointRegister.class.getName())
			);

		register.removeEntryPoint("/demo");
		logger.info("Removed demo application entry point");		
	}
	
};