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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ru.futurelink.mo.orm.Accessors;
import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.orm.ModelObject;
import ru.futurelink.mo.orm.dto.access.IDTOAccessChecker;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.exceptions.SaveException;
import ru.futurelink.mo.orm.security.User;

/**
 * @since 0.0
 */
public abstract class CommonDTO implements Serializable, IDTO {
	private static final long serialVersionUID = 1L;

	protected  ModelObject 		mData;
	protected	IDTOAccessChecker	mAccessChecker;
	
	public CommonDTO(ModelObject dataItem) {
		mData = dataItem;
	}
	
	public static List<?> fromResultList(List<?> resultList) {
		return null;
	}
	
	/**
	 * Прицепить элемент данных к DTO.
	 * 
	 * @param dataItem
	 */
	public void attachDataItem(ModelObject dataItem) {
		clearChangesBuffer();
		mData = dataItem;
	}

	public Class<? extends ModelObject> getDataClass() {
		if (mData != null)
			return mData.getClass();
		else 
			return null;
	}

	@Override
	public HashMap<String, Object[]> getChangesBuffer() {
		return null;
	}

	@Override
	public void clearChangesBuffer() {}
	
	@Override
	public void save() throws DTOException, SaveException {
		throw new SaveException("Saving is not implemented on CommonDTO.", null);
	}
	
	public void saveCommit() throws SaveException {
		throw new SaveException("Saving is not implemented on CommonDTO.", null);
	}
	
	@Override
	public String getId() throws DTOException {	
		Object idObj = getDataField("mId", "getId", "setId");
		if (idObj != null) {
			return idObj.toString();
		} else {
			return null;
		}	
	}
	
	@Override
	public boolean getDeleteFlag() throws DTOException {
		Object d = getDataField("mDeleteFlag", "getDeleteFlag", "setDeleteFlag");
		if (d != null) {
			return Boolean.valueOf(d.toString());
		} else {
			return false;
		}
	}	
	
	@Override
	public boolean equals(Object obj) {
		// Если есть реальный элемент данных - то сравниваем по нему и только,
		// а если нет - то по объекту.
		if ((mData != null) && (obj != null)) {
			return mData.equals(((CommonDTO)obj).mData);	
		} else {
			return super.equals(obj);
		}
	}

	@Override
	public void setDeleteFlag(boolean deleteFlag) throws DTOException {
		setDataField("mDeleteFlag", "getDeleteFlag", "setDeleteFlag", deleteFlag);
	}
	
	@Override
	public User getCreator() {
		if ((mData != null) && CommonObject.class.isAssignableFrom(mData.getClass())) {
			return ((CommonObject)mData).getCreator();
		}
		return null;
	}

	public void setDataField(String fieldName, Object data) throws DTOException {
		Accessors accessors = getAccessors(fieldName);		
		if (accessors != null && accessors.getter() != null && accessors.setter() != null) {
			setDataField(fieldName, accessors.getter(), accessors.setter(), data);
		} else {
			throw new DTOException("No getter or setter defined in annotation on setDataField.", null);
		}
	}

	public Object getDataField(String fieldName) throws DTOException {
		Accessors accessors = getAccessors(fieldName);
		if (accessors != null && accessors.getter() != null && accessors.setter() != null) {
			return getDataField(fieldName, accessors.getter(), accessors.setter());
		} else {
			throw new DTOException("No getter or setter defined in annotation on getDataField.", null);
		}
	}
	
	public Object getDataField(String fieldName, String fieldGetterName, String fieldSetterName) throws DTOException {
		return getDataField(fieldName, fieldGetterName, fieldSetterName, true);
	}

	public String getDataFieldAsString(String fieldName, String defaultValue) {
		Accessors accessors;
		try {
			accessors = getAccessors(fieldName);
		} catch (DTOException e) {
			return "[ Error getting accessors on field '"+fieldName+"']";
		}
		if (accessors != null && accessors.getter() != null) {
			return getDataFieldAsString(fieldName, accessors.getter(), defaultValue);
		} else {
			return "[ No getter in annotation ]";
		}
	}
	
	public String getDataFieldAsString(String fieldName, String getter, String defaultValue) {
		try {
			Object f = getDataField(fieldName, getter, null);
			if (f == null || f.equals("")) 
				return defaultValue;
			else
				return f.toString();
		} catch (DTOException ex) {
			return "[ Exception ]";
		}
	}

	/**
	 * Установить для DTO агента, проверяющего права доступа.
	 * 
	 * @param checker
	 */
	@Override
	public void addAccessChecker(IDTOAccessChecker checker) {
		mAccessChecker = checker;
	}
	
	/**
	 * Получить агент, проверяющий права доступа.
	 * 
	 * @return
	 */
	@Override
	public IDTOAccessChecker getAccessChecker() {
		return mAccessChecker;
	}
	
	@Override
	public ArrayList<String> getChangedData() throws DTOException { return null; }
	
	@Override
	public String toString() {
		StringBuffer str = new StringBuffer();

		try {
			if (getDataClass() != null)
				str.append("dataClass : ").append(getDataClass().getSimpleName()).append(", ");
			str.append("id : ").append(getId()).append(", ");
			str.append("deleteFlag : ").append(getDeleteFlag()).append(", ");
			str.append("changes buffer : ").append(getChangedData());
		} catch (DTOException e) {
			throw new RuntimeException(e);
		}
		
		return str.toString();
	}
	
	abstract public void refresh();
	
	private Accessors getAccessors(String fieldName) throws DTOException {
		Accessors accessors = null;
		try {
			accessors = mData.getAccessors(fieldName);
		} catch (NoSuchFieldException e) {
			throw new DTOException("No field named '"+fieldName+"' on setDataField.", e);
		} catch (SecurityException e) {
			throw new DTOException("Field '"+fieldName+"' security exception on setDataField.", e);
		}
		return accessors;
	}
}
