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

import ru.futurelink.mo.orm.dto.CommonDTOList;
import ru.futurelink.mo.orm.dto.FilterDTO;
import ru.futurelink.mo.orm.dto.IDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;

/**
 * @author pavlov
 *
 */
public interface IListComposite extends IComposite {
	public void setInput(CommonDTOList<? extends IDTO> input) throws DTOException;
	public void refresh() throws DTOException;
	
	public CommonDTOList<? extends IDTO> getDTO() throws DTOException;
	public void setActiveData(IDTO data);
	public IDTO getActiveData();

	public FilterDTO getFilter() throws DTOException;
	
	public void selectById(String id) throws DTOException;
	public void selectByDTO(IDTO dto);
}
