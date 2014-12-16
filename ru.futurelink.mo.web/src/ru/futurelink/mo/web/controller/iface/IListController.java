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

package ru.futurelink.mo.web.controller.iface;

import ru.futurelink.mo.orm.dto.IDTO;
import ru.futurelink.mo.orm.dto.access.IDTOAccessChecker;
import ru.futurelink.mo.orm.exceptions.DTOException;

/**
 * @author pavlov
 *
 */
public interface IListController
	extends IController
{
	
	/**
	 * Get access checker agent.
	 * 
	 * @return
	 */
	public IDTOAccessChecker getAccessChecker();
	
	/**
	 * Set access checker agent.
	 * 
	 * @param accessChecker
	 */
	public void setAccessCheker(IDTOAccessChecker accessChecker);
	
	/**
	 * Обработка запроса данных для списка.
	 */
	public void handleDataQuery() throws DTOException;

	/**
	 * Данные получены.
	 * @throws DTOException 
	 */
	public void handleDataQueryExecuted() throws DTOException;
	
	/**
	 * Выбрать элемент списка.
	 * 
	 * @param data
	 */
	public void setActiveData(IDTO data);

	/**
	 * Получить выбранный элемент.
	 * 
	 * @return
	 */
	public IDTO getActiveData();
}
