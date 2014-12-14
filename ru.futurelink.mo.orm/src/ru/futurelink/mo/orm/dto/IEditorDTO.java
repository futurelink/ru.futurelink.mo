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

import ru.futurelink.mo.orm.exceptions.DTOException;

/**
 * @author pavlov
 *
 */
public interface IEditorDTO extends IDTO {
	public void delete(boolean hardDelete) throws DTOException;
	public void recover() throws DTOException;
	
	public Long getCode();
	public void forceUpdateField(String field, CommonDTO dto);
	public void setDataField(String fieldName, String fieldGetterName,
			String fieldSetterName, Object value, boolean force) throws DTOException;
}
