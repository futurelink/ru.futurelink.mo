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

import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.exceptions.InitException;

/**
 * @author pavlov
 *
 */
public interface IListEditController extends IListController {
	/**
	 * Обработка создания нового элемента списка.
	 * @throws DTOException 
	 * @throws InitException 
	 */
	public void handleCreate() throws DTOException, InitException;
	
	/**
	 * Обработка удаления элемента.
	 * @throws DTOException 
	 */
	public void handleDelete() throws DTOException;
	
	/**
	 * Обработка восстановления элемента.
	 * 
	 * @throws DTOException
	 */
	public void handleRecover() throws DTOException;	
	
	/**
	 * Обработка редактирования элемента
	 * @throws DTOException 
	 */
	public void handleEdit() throws DTOException;
}
