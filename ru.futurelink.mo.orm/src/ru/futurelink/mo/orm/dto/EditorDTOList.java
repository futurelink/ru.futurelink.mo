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

package ru.futurelink.mo.orm.dto;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import ru.futurelink.mo.orm.ModelObject;
import ru.futurelink.mo.orm.dto.access.IDTOAccessChecker;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.iface.ICommonObject;
import ru.futurelink.mo.orm.pm.IPersistentManagerSession;

/**
 * @author pavlov
 *
 */
public class EditorDTOList<T extends CommonDTO> 
	extends CommonDTOList<T> {

	/**
	 * @param persistent
	 * @param accessChecker
	 * @param DTOclass
	 */
	public EditorDTOList(IPersistentManagerSession persistent,
			IDTOAccessChecker accessChecker, Class<T> DTOclass) {
		super(persistent, accessChecker, DTOclass);
	}

	/**
	 * Заполнить список элементами CommonObject.
	 * 
	 * @param sourceList
	 * @throws DTOException
	 */
	public void addObjectList(List<? extends ModelObject> sourceList) throws DTOException {
		if ((sourceList == null) || (sourceList.size() == 0)) {
			clear();
			return;
		} else {
			for (ModelObject object : sourceList) {
				if (ICommonObject.class.isAssignableFrom(object.getClass())) {
					((ICommonObject)object).setPersistentManagerSession(getPersistentManagerSession());
				}
				try {
					Constructor<T> ctr = getDTOClass().getConstructor(ModelObject.class);
					T dto = ctr.newInstance(object);
					if (dto != null) {
						if (dto.getAccessChecker() == null) dto.addAccessChecker(getAccessChecker());
						addDTOItem(dto);						
					} else {
						throw new DTOException("Ошибка, созданный DTO = null", null);
					}
				} catch (NoSuchMethodException | 
						SecurityException | 
						DTOException | 
						InstantiationException | 
						IllegalAccessException | 
						IllegalArgumentException | 
						InvocationTargetException e) {
					throw new DTOException("Невозможно заполнить объект списка DTO.", e);
				} 
			}		
		}		
	}
}
