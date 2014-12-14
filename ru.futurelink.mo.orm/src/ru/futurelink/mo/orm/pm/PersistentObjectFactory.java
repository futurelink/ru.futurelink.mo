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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;

import ru.futurelink.mo.orm.dto.IEditorDTO;
import ru.futurelink.mo.orm.dto.access.IDTOAccessChecker;
import ru.futurelink.mo.orm.iface.ICommonObject;
import ru.futurelink.mo.orm.iface.IModelObject;
import ru.futurelink.mo.orm.iface.IUser;

/**
 * @author pavlov
 *
 */
public class PersistentObjectFactory {

	private static PersistentObjectFactory INSTANCE = 
			new PersistentObjectFactory();

	private PersistentObjectFactory() {}

	public static PersistentObjectFactory getInstance() { return INSTANCE; }

	public IEditorDTO createEditorDTO(
			Class<? extends ICommonObject> dataClass,
			Class<? extends IEditorDTO> dtoClass,
			IPersistentManagerSession persistentManagerSession,
			IDTOAccessChecker accessChecker) {

		ICommonObject object = createPersistentObject(dataClass, persistentManagerSession);
		
		// Try to create DTO object of given type
		Constructor<? extends IEditorDTO> cons = null;
		try {
			cons = dtoClass.getConstructor(IModelObject.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}

		IEditorDTO dto = null ;
		try {
			dto = cons.newInstance(object);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}

		if (accessChecker != null)
			dto.addAccessChecker(accessChecker);

		return dto;		
	}

	public ICommonObject createPersistentObject(
			Class<? extends ICommonObject> clazz,
			IPersistentManagerSession persistentManagerSession) {

		Constructor<? extends ICommonObject> cons = null;
		try {
			cons = clazz.getConstructor(IPersistentManagerSession.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}

		ICommonObject object = null ;
		try {
			object = cons.newInstance(persistentManagerSession);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}

		// Creation datetime in server time zone.
		if (object.getCreateDate() == null) object.setCreateDate(Calendar.getInstance().getTime());

		if (PersistentManagerSessionUI.class.isAssignableFrom(persistentManagerSession.getClass())) {
			IUser accessUser = ((PersistentManagerSessionUI)persistentManagerSession).getAccessUser();
			IUser user = ((PersistentManagerSessionUI)persistentManagerSession).getUser();
			
			if (object.getOwner() == null) object.setOwner(accessUser);
			if (object.getCreator() == null) object.setCreator(user);
			if (object.getAuthor() == null) object.setAuthor(user);
		}
		
		return object;
	}
}
