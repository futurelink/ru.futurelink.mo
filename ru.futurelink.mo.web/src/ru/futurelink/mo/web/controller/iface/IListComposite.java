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

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.CommonDTOList;
import ru.futurelink.mo.orm.dto.FilterDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;

/**
 * @author pavlov
 *
 */
public interface IListComposite extends IComposite {
	public void setInput(CommonDTOList<? extends CommonDTO> input) throws DTOException;
	public void refresh() throws DTOException;
	
	public CommonDTOList<? extends CommonDTO> getDTO() throws DTOException;
	public void setActiveData(CommonDTO data);
	public CommonDTO getActiveData();

	public FilterDTO getFilter() throws DTOException;
	
	public void selectById(String id) throws DTOException;
	public void selectByDTO(CommonDTO dto);
}
