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

package ru.futurelink.mo.web.history;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.orm.dto.ViewerDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.table.CommonTable;
import ru.futurelink.mo.web.composites.table.CommonTableContentProvider;
import ru.futurelink.mo.web.controller.CompositeParams;

public class HistoryListTable extends CommonTable {

	private static final long serialVersionUID = 1L;

	public HistoryListTable(ApplicationSession session, Composite parent,
			int style, CompositeParams params) {
		super(session, parent, style, params);
	}

	@Override
	public void createTableColumns() {
		addColumn(getLocaleString("date"), 150, createLabelProvider(0));
		addColumn(getLocaleString("id"), 150, createLabelProvider(1));
		addColumn(getLocaleString("action"), 200, createLabelProvider(2));
	}

	@Override
	protected IContentProvider createContentProvider() {
		return new CommonTableContentProvider();
	}

	@Override
	protected ColumnLabelProvider createLabelProvider(int columnIndex) {
		return new HistoryLabelProvider(columnIndex);
	}
	
	/**
	 * Это провайдер ячеек вместе с их свойствами и данными.
	 * Провайдер получает DTO объект и выбирает нужные данные из него для отображения.
	 * 
	 * @author Futurelink
	 *
	 */
	private class HistoryLabelProvider extends ColumnLabelProvider {
		private static final long serialVersionUID = 1L;		
		private final int 		columnIndex;

		public HistoryLabelProvider( int columnIndex ) { this.columnIndex = columnIndex; }

	    @Override
	    public String getText( Object element ) {
	      ViewerDTO dto = (ViewerDTO)element;
	      String result = "-";
	      switch( columnIndex ) {
	        case 0:
	        	try {
	        		if (dto.getDataField("date") != null) {
	        			Date resultObj = (Date) dto.getDataField("date");
	        			SimpleDateFormat format = new SimpleDateFormat("dd.MM.y, hh:mm");
	        			result = resultObj != null ? format.format(resultObj).toString() : "";
	        		} else 
	        			result = "-";
	        	} catch (DTOException ex) {
	        		result = "!!!";
				}
	        	break;

	        case 1:
	        	try {
	        		if (dto.getDataField("objectId") != null) {
	        			result = dto.getDataField("mObjectId", "getObjectId", null).toString();
	        		}
	        	} catch (DTOException ex) {
	        		result = ex.toString();
	        	}
	        	break;

	        case 2:
	        	try {
	        		if (dto.getDataField("operation") != null) {
	        			result = dto.getDataField("id", "getOperation", "setOperation").toString();
	        		}
	        	} catch (DTOException ex) {
	        		result = ex.toString();
	        	}
	        	break;
	      }
	      return result;
	    }
	}

	@Override
	protected void itemSelected(Object item) {}

}
