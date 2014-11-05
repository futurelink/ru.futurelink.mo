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

package ru.futurelink.mo.web.composites.fields.datapicker;

import java.lang.reflect.Constructor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.CommonDTOList;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.table.CommonTableListener;
import ru.futurelink.mo.web.composites.table.ICommonTable;
import ru.futurelink.mo.web.controller.CommonListControllerListener;
import ru.futurelink.mo.web.controller.CompositeParams;
import ru.futurelink.mo.web.controller.CommonTableControllerListener;


/**
 * @author pavlov
 *
 */
public class SimpleDataPickerComposite 
	extends CommonDataPickerComposite {
	private static final long serialVersionUID = 1L;

	private Class<? extends ICommonTable>	mTableClass;
	private ICommonTable 					mTable;

	/**
	 * @param session
	 * @param parent
	 * @param style
	 * @param tableClass
	 * @param params
	 */
	public SimpleDataPickerComposite(ApplicationSession session,
			Composite parent, int style, Class<?> tableClass,
			CompositeParams params) {
		super(session, parent, style, tableClass, params);
	}

	@Override
	public void refresh() throws DTOException {
		setInput(getDTO());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected CommonComposite createWorkspace() {
		// Так как окно несколько нестандартно, мы создаем композит здесь, 
		// а не как положено в методе createWorkspace.
		try {
			mTableClass = (Class<? extends ICommonTable>) getParam("tableClass");
			Constructor<?> constr = mTableClass.getConstructor(
					ApplicationSession.class, 
					Composite.class, 
					int.class,
					CompositeParams.class);			
			mTable = (ICommonTable) constr.newInstance(getSession(), this, SWT.NONE, null);
			mTable.addTableListener(new CommonTableListener() {			
				@Override
				public void itemSelected(CommonDTO data) {
					setActiveData(data);
					if (getControllerListener() != null)
						((CommonListControllerListener)getControllerListener()).itemSelected(data);
				}
				
				@Override
				public void itemDoubleClicked(CommonDTO data) {
					setOwnerDialogResult("save");
					if (getControllerListener() != null) 
						((CommonListControllerListener)getControllerListener()).itemDoubleClicked(data);
				}

				@Override
				public void onColumnAdded(TableColumn column,
						String filterField, 
						String filterFieldGetter,
						String filterFieldSetter,
						Class<?> filterFieldType) {
					if (getControllerListener() != null)						
						((CommonTableControllerListener)getControllerListener()).onColumnAdded(
								column, 
								filterField, 
								filterFieldGetter, 
								filterFieldSetter,
								filterFieldType);					
				}

				@Override
				public void onColumnResized(TableColumn column) {
					if (getControllerListener() != null)
						((CommonTableControllerListener)getControllerListener()).onColumnResized(column);
				}
			});

			mWorkspace = (CommonComposite) mTable; // В воркспейсе - одна таблица
		} catch (Exception ex) {
			ex.printStackTrace();
		}		

		// Пакуем таблицу в качестве воркспейса
		if (mWorkspace != null) {
			mWorkspace.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | 
					GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | 
					GridData.GRAB_VERTICAL));
			mWorkspace.pack();
		}	

		return (CommonComposite) mTable;
	}

	public void createTableColumns() {
		mTable.initTable();
	}

	@Override
	public void setInput(CommonDTOList<? extends CommonDTO> input) throws DTOException {
		if (mTable != null)
			mTable.setInput(getDTO());		
	}
}
