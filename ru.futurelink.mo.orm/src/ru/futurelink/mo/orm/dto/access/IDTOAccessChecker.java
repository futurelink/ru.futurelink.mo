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

package ru.futurelink.mo.orm.dto.access;

import ru.futurelink.mo.orm.dto.CommonDTO;

/**
 * Интерфейс проверяльщика прав доступа для объекта DTO.
 * 
 * @author pavlov
 *
 */
public interface IDTOAccessChecker {
	
	public boolean checkCreate(CommonDTO dto);

	/**
	 * Проверка права на чтение объекта, получение данных.
	 * @param dto
	 * @return
	 */
	public boolean checkRead(CommonDTO dto, String fieldName);
	
	/**
	 * Проверка права на изменение объекта.
	 * @param dto
	 * @return
	 */
	public boolean checkWrite(CommonDTO dto, String fieldName);
	
	/**
	 * Проверка права на сохранение объкета.
	 * 
	 * @param dto
	 * @return
	 */
	public boolean checkSave(CommonDTO dto);
}
