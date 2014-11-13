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

package ru.futurelink.mo.orm.pm;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.UUID;
import java.util.Calendar;
import java.util.Map;

import javax.persistence.*;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.orm.HistoryObject;
import ru.futurelink.mo.orm.WorkLogSupport;
import ru.futurelink.mo.orm.exceptions.OpenException;
import ru.futurelink.mo.orm.exceptions.SaveException;

/**
 * Менеджер персистент объектов:
 * - знает o JPA-движке
 * - реализует прослойку между объектами в БД и кодом, который их использует
 * - управляет хранением объектов
 * 
 * @author pavlov_d
 * @since 0.1
 *
 */
public class PersistentManager {
	private EntityManagerFactory 	mFactory;
	private Logger					mLogger;

	private BundleContext			mBundleContext;
	private String					mPersistenceUnitName;
	private Dictionary<String, Object>	mProperties;
	
	@SuppressWarnings("unused")
	private PersistentManager() {}

	protected PersistentManager(String persistenceUnitName, Dictionary<String,Object> properties) {
		mLogger = LoggerFactory.getLogger(PersistentManager.class);
		mPersistenceUnitName = persistenceUnitName;
		mProperties = properties;
	}

	public PersistentManager(BundleContext context, String persistenceUnitName, Dictionary<String, Object> properties) {
		mBundleContext = context;
		mLogger = LoggerFactory.getLogger(PersistentManager.class);
		mPersistenceUnitName = persistenceUnitName;
		mProperties = properties;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public EntityManagerFactory getEntityManagerFactory(String unitName) {
		if (mFactory != null) return mFactory;
		
		ServiceReference[] refs = null;
	    try {
	    	logger().info("Ждем запуска PersistentManager для punit '{}'...", unitName);
	    	
	    	long deadline = System.currentTimeMillis() + 5000;
	    	synchronized(this) {
	    		do  {
	    			refs = mBundleContext.getServiceReferences(
	    					EntityManagerFactoryBuilder.class.getName(), 
	    					"(osgi.unit.name="+unitName+")"
	    					);
	    			Thread.sleep(500);
	    		} while ((refs == null) && (System.currentTimeMillis() < deadline)); 
	    	}
    		if (refs == null) {   			
    			throw new RuntimeException("Не могу получить EntityManagerFactoryBuilder!");
    		} else {	    		
    			EntityManagerFactoryBuilder emfb = (EntityManagerFactoryBuilder) mBundleContext.getService(refs[0]);
    			if (emfb != null) {    				 
    				// Переводим словарь в мап
    				Map<String, String> props = new HashMap<String, String>();
    				Enumeration<String> keys = mProperties.keys();
    				while (keys.hasMoreElements()) {
    					String key = keys.nextElement();
    			        props.put(key, (String) mProperties.get(key));
    			    }    				
    				return emfb.createEntityManagerFactory(props);
    			} else {
    				return null;
    			}
    		}
	    } catch (InvalidSyntaxException e) {
	        throw new RuntimeException("Services filtering syntax error", e);
	    } catch (InterruptedException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public Logger logger() {
		return mLogger;
	}
	
	protected synchronized EntityManager createEntityManager() {
		mFactory = getEntityManagerFactory(mPersistenceUnitName);
		EntityManager em = mFactory.createEntityManager();

		if (em == null) {
			throw new RuntimeException("Нет доступного EntityManager, вероятно фабрику создать не удалось!");
		} else
			return em;
	}

	/**
	 * Операция сохранения объекта на менеджере.
	 * @param object
	 * @return
	 * @throws SaveException
	 */
	protected	Object save(CommonObject object, PersistentManagerSession session) throws SaveException {
		int saveFlag = CommonObject.SAVE_CREATE;

		// Перед сохранением сохраняем неизмененную копию объекта
		// это актуально если объект сохраняется, а потом изменяется
		// тот же экземпляр и снова сохраняется.
		if (object.getId() != null) {
			object.setUnmodifiedObject(session.getOldEm().find(object.getClass(), object.getId()));
			object.setModifyDate(Calendar.getInstance().getTime());
			saveFlag = CommonObject.SAVE_MODIFY;
		}
		
		try {
			// Создатель элемента - по сути его владелец, в данном случае,
			// это пользователь в базе которого производится модификация.
			if (PersistentManagerSessionUI.class.isAssignableFrom(session.getClass())) {
				if (object.getCreator() == null) object.setCreator(((PersistentManagerSessionUI)session).getAccessUser());
				if (object.getAuthor() == null) object.setAuthor(((PersistentManagerSessionUI)session).getUser());
			}

			object.onBeforeSave(saveFlag);			// Вызов перед сохранением объекта
			
			// If couldn't begin new transaction or get existing we
			// throw SaveException to be correctly handled by controllers etc.
			try {
				session.transactionBegin();
			} catch (IllegalStateException ex) {
				throw new SaveException("Transaction begin exception", ex);
			}
		
			// Добавляем в ворклог данные о том, что элменет был изменен
			// у добавим ссылку на элемент ворклога в элемент данных.
			// Для элемента ворклога запись в ворклоге делать не надо!
			WorkLogSupport workLogObject = null;
			if (PersistentManagerSessionUI.class.isAssignableFrom(session.getClass())) {
				if (object.getWorkLogSupport()) {
					if (!object.getClass().getSimpleName().equals("WorkLogSupport")) {
						workLogObject = new WorkLogSupport(session);
						workLogObject.setCreateDate(Calendar.getInstance().getTime());
						workLogObject.setCreator(((PersistentManagerSessionUI)session).getUser());
						if (object.getId() == null) {
							workLogObject.setDescription("CREATED");
						} else {
							workLogObject.setDescription("MODIFIED");
						}
						session.getEm().persist(workLogObject);
						object.setWorkLog(workLogObject);
					}
				}
			}

			session.getEm().persist(object);					// ...

			object.onAfterSave(saveFlag);			// Вызов после сохранения объекта
			
			// После сохранения пересохраняем элемент ворклога,
			// добавляем туда имя класса и ИД объекта, к которому он
			// относится.
			if (workLogObject != null) {
				workLogObject.setObjectClassName(object.getClass().getName());
				workLogObject.setObjectId(object.getId());
				workLogObject.save();
			}			
		} catch (SaveException ex) {
			// Если во время сохранения возникла ошбика,
			// то откатываем установку изначального объекта, он
			// нам не нужен.
			object.setUnmodifiedObject(null);
			if (session.transactionIsOpened())
				session.transactionRollback();
			throw ex;								// Передаем наш ексепшн дальше
		}
		return null;
	}

	/**
	 * Сохранение объекта на менеджере с сохранением истории и изменением
	 * идентификатора объекта.
	 * @param object
	 * @param oldId
	 * @return
	 * @throws SaveException
	 */
	protected Object saveWithHistory(HistoryObject object, String oldId, PersistentManagerSession session) 
		throws SaveException, OpenException {
		// Записать ссылку на следующий объект в предыдущий
		// Записать ссылку на предыдущий объект в следующий
		// Создаем новый объект	
		int saveFlag = CommonObject.SAVE_CREATE;

		if (object != null) {
			try {
				object.onBeforeSave(saveFlag);					// Вызов перед сохранением объекта
			
				session.transactionBegin();
				session.getEm().detach(object);							// Отдетачиваем элемент от базы

				String newId = UUID.randomUUID().toString().toUpperCase();
				
				object.setId(newId);				// Сетим следующую ID
				object.setModifyDate(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime());
				object.setPrevId(oldId);			// Сохраняем старую ID

				// Создатель элемента - по сути его владелец, в данном случае,
				// это пользователь в базе которого производится модификация.
				if (PersistentManagerSessionUI.class.isAssignableFrom(session.getClass())) {
					if (object.getCreator() == null) object.setCreator(((PersistentManagerSessionUI)session).getAccessUser());
					if (object.getAuthor() == null) object.setAuthor(((PersistentManagerSessionUI)session).getUser());
				}
		
				// Добавляем в ворклог данные о том, что элменет был изменен
				// у добавим ссылку на элемент ворклога в элемент данных.
				// Для элемента ворклога запись в ворклоге делать не надо!
				WorkLogSupport workLogObject = null;
				if (PersistentManagerSessionUI.class.isAssignableFrom(session.getClass())) {
					if (object.getWorkLogSupport()) {				
						if (!object.getClass().getName().equals("WorkLog")) {
							workLogObject = new WorkLogSupport(session);
							workLogObject.setCreateDate(Calendar.getInstance().getTime());
							workLogObject.setCreator(((PersistentManagerSessionUI)session).getUser());
							workLogObject.setObjectId(newId);
							workLogObject.setDescription("MODIFIED");
							session.getEm().persist(workLogObject);
							object.setWorkLog(workLogObject);
						}
					}
				}
				
				session.getEm().merge(object);

				// Проставляем устаревание старому объекту
				HistoryObject oldObj = session.getEm().find(object.getClass(), oldId);
				oldObj.setOutdated(true);
				session.getEm().persist(oldObj);
				
				object.onAfterSave(saveFlag);			// Вызов после сохранения объекта
				
				// Открываем объект заново, чтобы был доступен,
				// потому что после detach-merge он слетает с управления
				// EntityManager.
				open(object.getClass(), newId, session);
				
				// После сохранения пересохраняем элемент ворклога,
				// добавляем туда имя класса и ИД объекта, к которому он
				// относится.
				if (workLogObject != null) {
					workLogObject.setObjectClassName(object.getClass().getName());
					workLogObject.setObjectId(object.getNextId());
					workLogObject.save();
				}			
			} catch (SaveException ex) {
				// Если во время сохранения возникла ошбика,
				// то откатываем установку изначального объекта, он
				// нам не нужен.
				object.setUnmodifiedObject(null);
				if (session.transactionIsOpened())
					session.transactionRollback();
				throw ex;								// Передаем наш ексепшн дальше
			}			
			return null;
		}
		return null;
	}
	
	/**
	 * Открыть объект из БД по ID.
	 * @param <T>
	 * @param id
	 * @return
	 */
	protected <T extends CommonObject> T open(Class<T> cls, String id, 
			PersistentManagerSession session) throws OpenException {
		T obj = session.getEm().find(cls, id);
		if (obj == null) throw new OpenException(id, "Элемент не найден.", null);
		
		obj.setUnmodifiedObject(session.getOldEm().find(cls, id));
		
		return  obj;
	}

	protected	Object delete(CommonObject object) {
		return null;
	}
	
	protected	Object onDelete() {
		return null;
	}
	
	protected	Object onDeleteError(String errorText) {
		return null;
	}
}
