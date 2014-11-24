package ru.futurelink.mo.orm.config;

import java.io.FileInputStream;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.Configuration;

public class Activator implements BundleActivator {
	private Logger			mLogger;
	private Configuration 	mConfig;

	public void start(BundleContext bundleContext) throws Exception {
		mLogger = LoggerFactory.getLogger(getClass());
		mLogger.info("Configuring ORM with ConfigAdmin");

		// Read config file
		String configPath = System.getProperty("ru.futurelink.mo.orm.config");
		mLogger.info("Reading configuration from {}", configPath);
		
		FileInputStream fis = new FileInputStream(configPath);
		Properties props = new Properties();
		props.load(fis);
		
		ServiceReference<ConfigurationAdmin> caRef = bundleContext.getServiceReference(ConfigurationAdmin.class);
		if (caRef == null) {
			throw new RuntimeException("ConfigAdmin is not run, can't configure ORM!");
		}
		
		try {
			synchronized (ConfigurationAdmin.class) {
				ConfigurationAdmin configAdmin = (ConfigurationAdmin)  bundleContext.getService(caRef);
		        mConfig = configAdmin.createFactoryConfiguration("gemini.jpa.punit", null);
		 
		        Dictionary<String, String> propsDict = new Hashtable<String, String>();

		        // Default properties
		        propsDict.put("gemini.jpa.punit.name", "mo");	        

			    propsDict.put("eclipselink.ddl-generation", "create-or-extend-tables");
			    propsDict.put("eclipselink.ddl-generation.output-mode", "database");
			    propsDict.put("eclipselink.session.customizer", "ru.futurelink.mo.orm.entities.helpers.UUIDSequence");

			    // Set loaded properties
		        for (final String name: props.stringPropertyNames())
		            propsDict.put(name, props.getProperty(name));
			    
		        mConfig.update(propsDict);				
			}
		} catch (Exception ex) {
		    ex.printStackTrace();
		    throw new RuntimeException(ex);
		}		
	}

	public void stop(BundleContext bundleContext) throws Exception {
		if (mConfig != null) {
			mConfig.delete();
			mConfig = null;
		}
	}

}
