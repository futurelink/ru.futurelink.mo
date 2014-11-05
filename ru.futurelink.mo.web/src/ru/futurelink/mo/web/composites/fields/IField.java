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

package ru.futurelink.mo.web.composites.fields;

import org.eclipse.swt.events.ModifyListener;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.controller.CommonControllerListener;
import ru.futurelink.mo.web.controller.CommonItemControllerListener;

/**
 * @author pavlov
 *
 */
public interface IField {

	public void addModifyListener(ModifyListener listener);

	public void setDataField(String dataField, String dataFieldGetter, String dataFieldSetter);
	public String getDataFieldName();
	public String getDataFieldGetter();
	public String getDataFieldSetter();
	
	/**
	 * Вычисление и получение значения поля типа, актуального для
	 * данного поля. Необходимо определить на раелизации поля ввода.
	 * 
	 * @return
	 */
	public Object getValue();
	
	public void setParentControllerListener(CommonItemControllerListener listener);
	
	public void setEditable(boolean isEditable);
	
	public boolean getEditable();
	
	public void refresh() throws DTOException;
	
	/**
	 * Получить элемент DTO для поля:
	 * - если используется в режиме с DataComposite - возвращается элемент DTO из окна отображения,
	 * - если используется с конструктором в который передан DTO - возвращается сохраненный DTO.
	 * @return
	 */
	public CommonDTO getDTO();
	
	/**
	 * Установить объект DTO для работы с полем.
	 * 
	 * @param dto
	 */
	public void setDTO(CommonDTO dto);
	
	/**
	 * Очистить поле до состояния по-умолчанию.
	 */
	public void clear();
	
	/**
	 * Установить признак обязательности поля.
	 *
	 * @param isMandatory
	 * @throws DTOException 
	 */
	public void setMandatory(boolean isMandatory);
	
	/**
	 * Получить признак обязательности поля.
	 * 
	 * @return
	 */
	public boolean getMandatory();

	/**
	 * @return
	 */
	public boolean isEmpty();
	
	public CommonControllerListener getControllerListener();
}
