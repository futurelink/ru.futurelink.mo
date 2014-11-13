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

package ru.futurelink.mo.orm.migration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import javax.persistence.TypedQuery;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import ru.futurelink.mo.orm.pm.PersistentManager;
import ru.futurelink.mo.orm.pm.PersistentManagerSession;

/**
 * Движок, который запускаем в работу миграции, от версии к версии.
 * Это сервис синглтон.
 * 
 * @author pavlov
 *
 */
public class MigrationEngine {
	private HashMap<Integer, HashMap<Class<? extends Migration>, Integer>> mMigrations;
	private PersistentManagerSession	persistentManagerSession;

	public MigrationEngine() {
		mMigrations = new HashMap<Integer, HashMap<Class<? extends Migration>, Integer>>();
	}

	public void start() {
		// Получим текущую версию из базы
		Integer currentVersion = 0;

		BundleContext mBundleContext = null;
		ServiceReference<PersistentManager> ref = null;
		if (FrameworkUtil.getBundle(getClass()).getState() == Bundle.ACTIVE) {
			mBundleContext = FrameworkUtil.getBundle(PersistentManager.class).getBundleContext();
			while (ref == null) { 
				ref = mBundleContext.getServiceReference(PersistentManager.class); 
				try { Thread.sleep(1000); } catch (InterruptedException ex) {}
			}
		} else {
			return;
		}

		PersistentManager persistentManager = (PersistentManager) mBundleContext.getService(ref);
		persistentManagerSession = new PersistentManagerSession(persistentManager); 
		
		TypedQuery<Integer> currentVersionQuery = persistentManagerSession.getEm().createQuery(
				"select max(version.mVersion) from MigrationVersion version", Integer.class);
		if (currentVersionQuery.getResultList().size() > 0) {
			if (currentVersionQuery.getResultList().get(0) != null)
				currentVersion = currentVersionQuery.getResultList().get(0);
		}
		
		persistentManager.logger().info("Текущая версия базы: {}", currentVersion);
		
		// Получим экземпляр класса миграции по версии
		HashMap<Class<? extends Migration>, Integer> migrationsFromVersion = mMigrations.get(currentVersion);
		if (migrationsFromVersion == null) {
			persistentManager.logger().info("Нет доступных миграций с этой версии базы данных. Завершено.");
			return;
		}
		
		for (Class<? extends Migration> migrationClass : migrationsFromVersion.keySet()) {
			// Если у нас несколько миграций и они будут друг за другом изменять версии,
			// то тут они все не пройдут, пройдет только первая, но если миграций несколько
			// а версию они не меняют - то в этом случае будет все в порядке и они применятся
			// все.
			persistentManager.logger().info("Получили миграцию с реализацией в {}", migrationClass.getName());
			
			Integer versionTo = migrationsFromVersion.get(migrationClass);
			
			Migration migrationInstance;
			try {
				Constructor<? extends Migration> ctr = (Constructor<? extends Migration>) migrationClass.getConstructor(
					PersistentManagerSession.class, int.class, int.class);
				migrationInstance = ctr.newInstance(persistentManagerSession, currentVersion, versionTo);
			} catch (IllegalAccessException | 
					NoSuchMethodException | 
					SecurityException | 
					InstantiationException | 
					IllegalArgumentException | 
					InvocationTargetException ex) {
				throw new RuntimeException(ex);
			}

			migrationInstance.start("NO REVISION YET");
			
			persistentManager.logger().info("Отработали миграцию с реализцией в {}", migrationClass.getName());
		}
		
		persistentManager.logger().info("Все миграции отработаны.");
	}
	
	public void addMigration(Class<? extends Migration> migrationClass, Integer versionFrom, Integer versionTo) {
		HashMap<Class<? extends Migration>, Integer> migration = new HashMap<Class<? extends Migration>, Integer>();
		migration.put(migrationClass, versionTo);
		mMigrations.put(versionFrom, migration);
	}
}
