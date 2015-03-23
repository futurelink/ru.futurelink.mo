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

package ru.futurelink.mo.web.composites.fields.datapicker.as;

import org.eclipse.jface.viewers.LabelProvider;

import ru.futurelink.mo.orm.dto.IDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;

/**
 * List label provider for DTO. Displays one column of
 * DTO as text.
 * 
 * @author pavlov
 *
 */
public class ListLabelProvider extends LabelProvider {
	private static final long serialVersionUID = 1L;

	private String displayColumnName;
	
	public ListLabelProvider(String displayColumnName) {
		this.displayColumnName = displayColumnName;
	}
	
	@Override
	public String getText(Object element) {		
		try {
			Object field = ((IDTO)element).getDataField(displayColumnName);
			if (field != null)
				return field.toString();
			return "-";
		} catch (DTOException ex) {
			return ex.toString();
		}
	}
}
