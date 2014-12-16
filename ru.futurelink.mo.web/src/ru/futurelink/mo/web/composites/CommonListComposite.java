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

package ru.futurelink.mo.web.composites;

import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.CommonDTOList;
import ru.futurelink.mo.orm.dto.EditorDTOList;
import ru.futurelink.mo.orm.dto.FilterDTO;
import ru.futurelink.mo.orm.dto.IDTO;
import ru.futurelink.mo.orm.dto.access.AllowOwnChecker;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.controller.CompositeParams;
import ru.futurelink.mo.web.controller.iface.IListComposite;
import ru.futurelink.mo.web.controller.iface.ListDTOAccessor;

/**
 * <p>Composite to work with list data sets.</p>
 *
 * <p>This composite has CommonDTOList object which contains a set of DTO objects, each of them is an
 * item of the data list to deal with.</p>
 *
 * @author pavlov
 * @param <T>
 *
 */
abstract public class CommonListComposite 
	extends CommonDataComposite
	implements IListComposite {

	private static final long serialVersionUID = 1L;

	protected CommonDTOList<? extends IDTO>	dto;
	private IDTO								activeData;
	
	public CommonListComposite(ApplicationSession session,
			Composite parent, int style, CompositeParams params) {
		super(session, parent, style, params);

        // Create default editor DTO list with default access checker
		dto = new EditorDTOList<CommonDTO>(getSession().persistent(),
            new AllowOwnChecker(getSession().getUser()), CommonDTO.class
        );
	}

	/**
	 * Link CommonDTOList object to this composite.
	 * 
	 * @param data коллекция CommonDTO
	 * @throws DTOException 
	 */
	protected void attachDTO(CommonDTOList<? extends IDTO> data) throws DTOException {
		dto = data;				
		refresh();
	}

	/**
	 * Unlink and free CommonDTOList linked to composite.
	 * @throws DTOException 
	 */
	protected void removeDTO() throws DTOException {
		if (dto != null) {		
			dto = null;			
			refresh();
		}
	}
	
	/**
     * <p>Get DTO list:</p>
     * <ul>
     * <li>1) attached to list view if controller listener is not assigned</li>
     * <li>2) attached to list controller if controller listener is assigned via its
     *    getControllerDTO() method. Controller listener must implement ListDTOAccessor
     *    interface.</li>
     * </ul>
     *
	 * @return - CommonDTOList instance
	 */
	@Override
	public CommonDTOList<? extends IDTO> getDTO() throws DTOException {
		if (getControllerListener() != null) {
			
			// Если у нас обработчик контроллера не кастуется в обработчик контроллера списка,
			// то надо обработать это и вывалить эксепшн.
			if (!(ListDTOAccessor.class.isAssignableFrom(getControllerListener().getClass()))) {
				throw new DTOException(getErrorString("invalidHandlerOnListController"), null);
			}

			if (((ListDTOAccessor)getControllerListener()).getControllerDTO() != null) {
				return ((ListDTOAccessor)getControllerListener()).getControllerDTO();
			}
		}

		return dto;
	}

	/**
	 * Get FilterDTO object which desribes filter conditions to be applied to list view.
	 * 
	 * @return объект FilterDTO
	 * @throws DTOException
	 */
	@Override
	public FilterDTO getFilter() throws DTOException {
		if (getParam("filter") != null)
			return (FilterDTO)getParam("filter");
		else
			throw new DTOException(getErrorString("noFilterObject"), null);
	}

	/**
	 * Set active (selected) item in list view.
	 *
	 * @param data element from CommonDTOList attached to list view
	 */
	@Override
	public void setActiveData(IDTO data) {
		activeData = data;
	}

	/**
	 * Get active (selected) item from list view.
	 * 
	 * @return
	 */
	@Override
	public IDTO getActiveData() {
		return activeData;
	}

}
