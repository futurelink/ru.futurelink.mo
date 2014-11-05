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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.orm.ModelObject;
import ru.futurelink.mo.orm.dto.access.IDTOAccessChecker;
import ru.futurelink.mo.orm.exceptions.DTOException;

public class ViewerDTO extends CommonDTO {

	private static final long serialVersionUID = 1L;

	public ViewerDTO(ModelObject data) {
		super(data);
	}
	
	/**
	 * Метод создает список DTO из списка элементов типа CommonObject.
	 * @param resultList
	 * @return
	 */
	public static Map<String, ViewerDTO> fromResultList(List<?> resultList, IDTOAccessChecker accessChecker) {
		if ((resultList != null) && (resultList.size() > 0)) {
			Map<String, ViewerDTO> list = new HashMap<String, ViewerDTO>();
			for (Object c : resultList) {
				ViewerDTO dto = new ViewerDTO((ModelObject)c);
				dto.addAccessChecker(accessChecker);
				list.put(((CommonObject)c).getId(), dto);
			}
			return list;
		} else {
			return null;
		}
	}

	@Override
	public Object getDataField(String fieldName, String fieldGetterName,
			String fieldSetterName, boolean checkAccess) throws DTOException {
		if (mAccessChecker == null && checkAccess) {
			throw new DTOException("Не установлен агент проверки прав доступа, операция невозможна", null);
		}

		Object a = null;

		// Если пытаются получить данные, а объекта
		// ОРМ просто нет, то выкидываем специфический эксепшн.
		if (mData == null)
			throw new DTOException("Элемент данных = null", null);
		
		try {
			Class<?> fieldClass = mData.getClass().getMethod(fieldGetterName).getReturnType();
	        Method getValueMethod = mData.getClass().getMethod(fieldGetterName);
	        if (CommonObject.class.isAssignableFrom(fieldClass)) {
	        	// Нужно генерить DTO на основании объекта только тогда,
	        	// когда этот объект, то есть значение поля в ORM не null.
	        	CommonObject obj = (CommonObject) getValueMethod.invoke(mData);
	        	if (obj != null) {
		        	a = new EditorDTO((CommonObject)obj);
		        	((CommonDTO)a).addAccessChecker(mAccessChecker);
	        	}
	        } else {
	        	a = getValueMethod.invoke(mData);
	        }
		} catch (Exception e) {
			throw new DTOException(e.toString(), e);
		}
		
		return a;
	}

	@Override
	public void setDataField(String fieldName, String fieldGetterName,
			String fieldSetterName, Object value) throws DTOException {
		throw new DTOException("Это объект ViewerDTO, он не предназначен для изменения данных!", null);
	}

	@Override
	public void clear() {
		mData = null;
	}

	@Override
	public void refresh() {

	}
}
