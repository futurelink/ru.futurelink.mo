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

import java.util.Collection;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import ru.futurelink.mo.orm.migration.MigrationEngine;
import ru.futurelink.mo.orm.pm.PersistentManager;
import ru.futurelink.mo.orm.pm.PersistentObjectFactory;

/**
 * Активатор запускает процедуру миграции.
 * 
 * @author pavlov
 *
 */
public class Activator implements BundleActivator {

	private MigrationEngine 		migrationEngine = null;
	private PersistentManager		persistentManager = null;
	private Logger					logger = null;
	private BundleContext			context;	
	private static String			PERSISTENT_UNIT_NAME = "mo";

	@Override
	public void start(BundleContext ctx) throws Exception {
		this.context = ctx;

		logger  = LoggerFactory.getLogger(this.getClass());

		// Register persistent object factory service
		context.registerService(PersistentObjectFactory.class, 
				PersistentObjectFactory.getInstance(), null);

		if (context.getServiceReference(PersistentManager.class) == null) {
			new Thread() {
				public void run() {
					logger.info("Registering persistent manager service");
					
					persistentManager = new PersistentManager(
						context, 
						PERSISTENT_UNIT_NAME,
						null);

					context.registerService(PersistentManager.class, 
							persistentManager, null);

					if (context.getServiceReference(MigrationEngine.class) == null) {
						logger.info("Registering migrations service");
						migrationEngine = new MigrationEngine();
						context.registerService(MigrationEngine.class,
								migrationEngine, null);
					}	
				}
			}.start();
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		logger.info("Removing PersistentManager and MigrationEngine services");

		Collection<ServiceReference<MigrationEngine>> refs = null;
		refs = context.getServiceReferences(MigrationEngine.class, null);
		for (ServiceReference<?> ref : refs) {
			context.ungetService(ref);
		}

		if (context.getServiceReference(PersistentManager.class) != null) {
			context.ungetService(context.getServiceReference(PersistentManager.class));
		}

		if (context.getServiceReference(PersistentObjectFactory.class) != null) {
			context.ungetService(context.getServiceReference(PersistentObjectFactory.class));
		}

		persistentManager = null;
		migrationEngine = null;
	}

}
