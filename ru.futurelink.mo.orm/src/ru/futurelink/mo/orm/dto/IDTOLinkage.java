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

import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.security.User;

/**
 * @author pavlov
 *
 */
public interface IDTOLinkage {
	/**
	 * Задать объект данных для подписки.
	 * 
	 * @param linkageItem
	 */
	public void setLinkageItem(CommonObject linkageItem);
	
	/**
	 * Получить DTO объект подписки.
	 * 
	 * @return
	 */
	public EditorDTO getLinkageDTO();
	
	/**
	 * Активировать (восстановить) подписку.
	 * 
	 * @throws DTOException
	 */
	public void activateLinkage() throws DTOException;
	
	/**
	 * Деактивировать (удалить) подписку.
	 * 
	 * @throws DTOException
	 */
	public void deactivateLinkage() throws DTOException;
	
	/**
	 * Получить пометку об активности/неактивности (удалении) подписки.
	 * 
	 * @return
	 * @throws DTOException
	 */
	public Boolean getLinkageActive() throws DTOException;
	
	/**
	 * Узнать, сохранена ли подписка в БД,
	 * 
	 * @return
	 * @throws DTOException
	 */
	public Boolean getLinkagePersisted() throws DTOException;
	
	/**
	 * Получить создателя подписки.
	 * 
	 * @return
	 */
	public User getLinkageCreator();
}
