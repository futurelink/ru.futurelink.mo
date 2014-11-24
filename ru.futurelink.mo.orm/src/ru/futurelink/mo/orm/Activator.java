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

package ru.futurelink.mo.orm;

import java.io.IOException;
import java.util.Collection;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import ru.futurelink.mo.orm.migration.MigrationEngine;
import ru.futurelink.mo.orm.pm.PersistentManager;

/**
 * Активатор запускает процедуру миграции.
 * 
 * @author pavlov
 *
 */
public class Activator implements BundleActivator {

	private MigrationEngine 		mMigrationEngine = null;
	private PersistentManager		mPersistentManager = null;
	private Logger					mLogger = null;
	private BundleContext			mContext;	
	private static String			PERSISTENT_UNIT_NAME = "mo";

	@Override
	public void start(BundleContext context) throws Exception {
		
		mLogger  = LoggerFactory.getLogger(this.getClass());

		/*
		 *  Зарегистрируем сервис миграционного движка и персистента.
		 *  Такой сервис может быть только один! И следующий тоже.
		 */	
		if (context.getServiceReference(PersistentManager.class) == null) {
			mContext = context;
			new Thread() {
				public void run() {
					mLogger.info("Registering persistent manager service");

					// Получаем конфигруацию из нужного нам сервиса и передаем ее...
					ServiceReference<ConfigurationAdmin> caRef = mContext.getServiceReference(ConfigurationAdmin.class);
					if (caRef == null) {
						throw new RuntimeException("ConfigAdmin не запущен, конфигурация JPA невозможна");
					}
			
					ConfigurationAdmin configAdmin = (ConfigurationAdmin)  mContext.getService(caRef);
					Configuration[] config = null;				

					do {
						synchronized (ConfigurationAdmin.class) {
							try {
								config = configAdmin.listConfigurations("(service.factoryPid=gemini.jpa.punit)");
								if (config == null) {
									mLogger.info("Конфигурация Gemini не доступна, повтор через 3 сек.");
									Thread.sleep(3000);
								}
							} catch (InterruptedException ex) {
								mLogger.warn("Попытка отыскать Gemini была прервана.");
							} catch (IOException | InvalidSyntaxException ex) {
								mLogger.error("Попытка отыскать Gemini завершилась ошибкой", ex);
							}
						}
					} while (config == null);

					mPersistentManager = new PersistentManager(
						mContext, 
						PERSISTENT_UNIT_NAME, 
						config[config.length-1].getProperties());
					mContext.registerService(PersistentManager.class, 
							mPersistentManager, null);
				}
			}.start();
		}

		if (context.getServiceReference(MigrationEngine.class) == null) {
			mLogger.info("Регистрация сервися миграций");
			mMigrationEngine = new MigrationEngine();
			context.registerService(MigrationEngine.class,
					mMigrationEngine, null);
		}	
	}

	@Override
	public void stop(BundleContext arg0) throws Exception {
		mLogger.info("Убираем регистрацию сервисов PersistentManager и MigrationEngine");

		Collection<ServiceReference<MigrationEngine>> refs = null;
		refs = arg0.getServiceReferences(MigrationEngine.class, null);
		for (ServiceReference<?> ref : refs) {
			arg0.ungetService(ref);
		}

		if (arg0.getServiceReference(PersistentManager.class) != null) {
			arg0.ungetService(arg0.getServiceReference(PersistentManager.class));
		}

		mPersistentManager = null;
		mMigrationEngine = null;
	}

}
