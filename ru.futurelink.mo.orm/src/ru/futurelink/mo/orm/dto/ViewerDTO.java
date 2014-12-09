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

import ru.futurelink.mo.orm.ModelObject;
import ru.futurelink.mo.orm.dto.access.IDTOAccessChecker;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.iface.ICommonObject;

/**
 * Simple DTO for viewing data only.
 *
 * @author pavlov
 */
public class ViewerDTO extends CommonDTO {

	private static final long serialVersionUID = 1L;

	public ViewerDTO(ModelObject data) {
		super(data);
	}
	
	/**
	 * @param resultList
	 * @return
	 */
    @Deprecated
	public static Map<String, ViewerDTO> fromResultList(List<?> resultList, IDTOAccessChecker accessChecker) {
		if ((resultList != null) && (resultList.size() > 0)) {
			Map<String, ViewerDTO> list = new HashMap<String, ViewerDTO>();
			for (Object c : resultList) {
				ViewerDTO dto = new ViewerDTO((ModelObject)c);
				dto.addAccessChecker(accessChecker);
				list.put(((ICommonObject)c).getId(), dto);
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
			throw new DTOException("Data access checker agent is not set to DTO, operation not allowed.", null);
		}

		Object a = null;

		if (mData == null)
			throw new DTOException("Data model object can not be null", null);

		try {
			Class<?> fieldClass = mData.getClass().getMethod(fieldGetterName).getReturnType();
	        Method getValueMethod = mData.getClass().getMethod(fieldGetterName);
            // If field is an object type - create ViewerDTO and set access checker
	        if (ICommonObject.class.isAssignableFrom(fieldClass)) {
	        	ModelObject obj = (ModelObject) getValueMethod.invoke(mData);
	        	if (obj != null) {
		        	a = new ViewerDTO((ModelObject)obj);
		        	((IDTO)a).addAccessChecker(mAccessChecker);
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
		throw new DTOException("ViewerDTO doesn't support data changes!", null);
	}

	@Override
	public void clear() {
		mData = null;
	}

	@Override
	public void refresh() {}
}
