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

package ru.futurelink.mo.web.composites.table;

import org.eclipse.jface.viewers.IContentProvider;

import ru.futurelink.mo.orm.dto.CommonDTOList;
import ru.futurelink.mo.orm.dto.IDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;

/**
 * Interface for all table implementation to use with this framework.
 *
 * @author pavlov
 *
 */
public interface ICommonTable {
    /**
     * Set input data set.
     *
     * @param data
     */
	public void setInput(CommonDTOList<? extends IDTO> data);

    /**
     * Get input data set.
     *
     * @return
     */
	public Object getInput();
	
	public void setContentProvider(IContentProvider provider);
	public void setRowHeight(Integer height);
	
	public void selectById(String id) throws DTOException;
	public void selectByDTO(IDTO dto);
	
	public void addTableListener(CommonTableListener listener);
	public void initTable();
	
	public void setLayoutData(Object gridData);
	
	/**
	 * Refresh table contents.
	 */
	public void refresh();
	
	/**
     * Inner method to create table columns. For JFace TableViewer it calls addColumn().
	 */
	public void createTableColumns();	
}
