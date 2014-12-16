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

import java.lang.reflect.Constructor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

import ru.futurelink.mo.orm.dto.CommonDTOList;
import ru.futurelink.mo.orm.dto.IDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.table.CommonTableListener;
import ru.futurelink.mo.web.composites.table.ICommonTable;
import ru.futurelink.mo.web.composites.toolbar.CommonToolbar;
import ru.futurelink.mo.web.composites.toolbar.JournalToolbar;
import ru.futurelink.mo.web.composites.toolbar.ToolbarListener;
import ru.futurelink.mo.web.controller.CommonTableControllerListener;
import ru.futurelink.mo.web.controller.CompositeParams;
import ru.futurelink.mo.web.controller.CommonListControllerListener;
import ru.futurelink.mo.web.exceptions.CreationException;
import ru.futurelink.mo.web.exceptions.InitException;

/**
 * <p><b>Simplified list composite.</b></p>
 *
 * <p>This class is provided for most common lists and operation on lists.
 * SimpleListComposite is created by SimpleListController and configured by
 * CompositeParams set on controller from which it's created.</p>
 *
 * <p>This list implementation handles by default:
 * <ul>
 *     <li>creation of items</li>
 *     <li>edit of items (doubleclick on item or by toolbar button)</li>
 *     <li>deletion of items</li>
 * </ul>
 * It also supports view filtering by FilterDTO.
 * </p>
 *
 * <p>The only thing you need to implement it is an ICommonTable defining your table view
 * (columns, style etc.) and visual component.</p>
 *
 * <p>CompositeParams parameter set which handled by SimpleListComposite:
 * <ul>
 *     <li><b>tableClass</b> is your ICommonTable implementation</li>
 * </ul></p>
 *
 * @author pavlov
 * @since 1.2
 *
 */
public class SimpleListComposite extends CommonListComposite {

	private static final long serialVersionUID = 1L;

	protected	ICommonTable	table;
	private		Class<?>		tableClass;
	
	public SimpleListComposite(ApplicationSession session, Composite parent, int style, CompositeParams params) {
		super(session, parent, style, params);
	}

	@Override
	public void refresh() throws DTOException {
		if (getDTO() != null)
			setInput(getDTO());
	}

	@Override
	protected CommonComposite createWorkspace() throws CreationException {
		tableClass = (Class<?>) getParam("tableClass");
		if (tableClass == null) {
			throw new CreationException(getErrorString("noTableClassInSimpleList"));
		}

		try {
			Constructor<?> constr = tableClass.getConstructor(
					ApplicationSession.class, 
					Composite.class, 
					int.class, 
					CompositeParams.class);
			table = (ICommonTable) constr.newInstance(getSession(), this, SWT.NONE, null);
			table.addTableListener(new CommonTableListener() {			
				@Override
				public void itemSelected(IDTO data) {
					setActiveData(data);
					if (getControllerListener() != null)
						((CommonListControllerListener)getControllerListener()).itemSelected(data);
				}
				
				@Override
				public void itemDoubleClicked(IDTO data) {
					if (getControllerListener() != null)
						((CommonListControllerListener)getControllerListener()).itemDoubleClicked(data);
				}

				@Override
				public void onColumnResized(TableColumn column) {
					if (getControllerListener() != null)
						((CommonTableControllerListener)getControllerListener()).onColumnResized(column);
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
			});
		} catch (Exception ex) {
			getControllerListener().sendError(getErrorString("creationErrorInSimpleList"), ex);
		}		

		return (CommonComposite)table;
	}

	// Создание колонок таблицы надо вызывать отдельно, чтобы был уже привязан
	// обработчик событий от таблицы.
	public void createTableColumns() {
		((ICommonTable)table).createTableColumns();
	}

	@Override
	protected CommonToolbar createToolbar() {
		JournalToolbar toolbar = new JournalToolbar(getSession(), this, SWT.NONE, null);
		toolbar.addToolBarListener(new ToolbarListener() {
			
			@Override
			public void toolBarButtonPressed(Button button) {
				try {
					if (button.getData().toString().equals("create")) {
						((CommonListControllerListener)getControllerListener()).create();
					} else if (button.getData().toString().equals("edit")) {
						((CommonListControllerListener)getControllerListener()).edit();
					} else if (button.getData().toString().equals("delete")) {
						((CommonListControllerListener)getControllerListener()).delete();
					}
				} catch (DTOException | InitException ex) {
					getControllerListener().sendError(getErrorString("dtoException"), ex);
					return;
				}
			}
		});
		return toolbar;
	}

	@Override
	public void selectById(String id) throws DTOException {
		table.selectById(id);
	}

	@Override
	public void selectByDTO(IDTO dto) {
		table.selectByDTO(dto);
	}

	public ICommonTable getTable() {
		return table;
	}

	@Override
	public void setInput(CommonDTOList<? extends IDTO> input) {
		if (table != null)
			table.setInput(input);		
	}

}
