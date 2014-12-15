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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.futurelink.mo.orm.ModelObject;
import ru.futurelink.mo.orm.annotations.Accessors;
import ru.futurelink.mo.orm.dto.access.IDTOAccessChecker;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.exceptions.SaveException;
import ru.futurelink.mo.orm.iface.ICommonObject;
import ru.futurelink.mo.orm.iface.IModelObject;
import ru.futurelink.mo.orm.iface.IUser;

/**
 * @author pavlov_d
 */
public abstract class CommonDTO implements Serializable, IDTO {
	private static final long serialVersionUID = 1L;

	protected	IModelObject 		mData;
	private		IDTOAccessChecker	mAccessChecker;
	private		Logger				logger;
	
	public CommonDTO(IModelObject dataItem) {
		mData = dataItem;
	}

	protected Logger logger() {
		if (logger == null)
			logger = LoggerFactory.getLogger(getClass());

		return logger;
	}
	
	public static List<?> fromResultList(List<?> resultList) {
		return null;
	}
	
	/**
	 * Attach data model to DTO.
	 * 
	 * @param dataItem
	 */
	public void attachDataItem(ModelObject dataItem) {
		clearChangesBuffer();
		mData = dataItem;
	}

    /**
     * Get model object data class.
     *
     * @return
     */
	@Override
	public Class<? extends IModelObject> getDataClass() {
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

	@Override
	public void saveCommit() throws SaveException {
		throw new SaveException("Saving is not implemented on CommonDTO.", null);
	}
	
	@Override
	public String getId() throws DTOException {	
		Object idObj = getDataField(ModelObject.FIELD_ID);
		if (idObj != null) {
			return idObj.toString();
		} else {
			return null;
		}	
	}
	
	@Override
	public boolean getDeleteFlag() throws DTOException {
		Object d = getDataField(ModelObject.FIELD_DELETEFLAG);
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
		setDataField(ModelObject.FIELD_DELETEFLAG, deleteFlag);
	}
	
	@Override
	public IUser getCreator() {
		if ((mData != null) && ICommonObject.class.isAssignableFrom(mData.getClass())) {
			return ((ICommonObject)mData).getCreator();
		}
		return null;
	}

	@Override
	public IUser getOwner() {
		if ((mData != null) && ICommonObject.class.isAssignableFrom(mData.getClass())) {
			return ((ICommonObject)mData).getOwner();
		}
		return null;
	}

    /**
     * Set data field convinience method. Field setter and getter methods are
     * aquired from @Accessors annotation on data field in model class definition.
     *
     * @param fieldName
     * @param data
     * @throws DTOException
     */
	public void setDataField(String fieldName, Object data) throws DTOException {
		Accessors accessors = getAccessors(fieldName);		
		if (accessors != null && accessors.getter() != null && accessors.setter() != null) {
			setDataField(fieldName, accessors.getter(), accessors.setter(), data);
		} else {
			throw new DTOException("No getter or setter defined in annotation on setDataField.", null);
		}
	}

    /**
     * Get data field value convinience method. Field setter and getter methods are
     * aquired from @Accessors annotation on data field in model class definition.
     *
     * @param fieldName
     * @return
     * @throws DTOException
     */
	@Override
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

    /**
     * Get data field value and convert it to string value. This method is implemeneted for
     * convinience. Field setter and getter methods are aquired from @Accessors
     * annotation on data field in model class definition.
     *
     * @param fieldName
     * @param defaultValue
     * @return
     */
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

	@Override
	public void addAccessChecker(IDTOAccessChecker checker) {
		mAccessChecker = checker;
	}
	
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

    /**
     * Get data model field accessors from @Accessors annotation for this field.
     *
     * @param fieldName
     * @return
     * @throws DTOException
     */
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
