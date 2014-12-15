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

import java.util.ArrayList;

import ru.futurelink.mo.orm.dto.IDTO;
import ru.futurelink.mo.orm.dto.access.IDTOAccessChecker;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.exceptions.OpenException;
import ru.futurelink.mo.orm.exceptions.SaveException;
import ru.futurelink.mo.orm.exceptions.ValidationException;
import ru.futurelink.mo.orm.iface.ICommonObject;
import ru.futurelink.mo.web.controller.RelatedController;

/**
 * @author pavlov
 *
 */
public interface IItemController 
	extends IController {

	/* Методы для работы с DTO */

	public IDTO getDTO();
	public void setDTO(IDTO dto)  throws DTOException;

	public IDTOAccessChecker createAccessChecker();
	
	/* Методы манипуляции объъектом */

	public void create() throws DTOException;
	public void openById(String id) throws OpenException;
	public void open(ICommonObject data) throws OpenException;
	public void save() throws SaveException, DTOException, ValidationException;
	public void saveCommit() throws SaveException;
	public void close();

	public void revertChanges() throws DTOException;

	/* События об операциях с объектом */

	public void doAfterCreate();	
	public void doAfterOpen() throws OpenException;
	
	public void doBeforeSave() throws SaveException;
	public void doAfterSave() throws SaveException;

	/* Разные другие методы */

	public ArrayList<String> getDataChanged() throws DTOException;
	public boolean getRelatedDataChanged();

	public void addRelatedController(RelatedController ctrl);
}
