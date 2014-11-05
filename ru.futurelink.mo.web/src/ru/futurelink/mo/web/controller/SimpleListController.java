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

package ru.futurelink.mo.web.controller;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.EditorDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.exceptions.OpenException;
import ru.futurelink.mo.orm.exceptions.SaveException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.IDragDropDecorator;
import ru.futurelink.mo.web.composites.SimpleListComposite;
import ru.futurelink.mo.web.composites.CommonListComposite;
import ru.futurelink.mo.web.composites.dialogs.CommonItemDialog;
import ru.futurelink.mo.web.controller.iface.ICompositeController;
import ru.futurelink.mo.web.controller.iface.IListEditController;
import ru.futurelink.mo.web.exceptions.InitException;

/**
 * <p>Simmplified list controller for SimpleListComposite. It's easy to configure and use.</p>
 * <p>Subclass it and implement handleDataQuery() to retrieve data from data source and call
 * handleDataQueryExecuted() after.</p>
 *
 * <p>CompositeParams parameter set which handled by SimpleListComposite:
 * <ul>
 *     <li><b>listComposite</b> custom list composite to use with this controller inherited from SimpleListComposite</li>
 *     <li><b>itemEditMode</b> item edit mode (CONTAINER or DIALOG)</li>
 *     <li><b>itemEditContainer</b> if mode is CONTAINER it may be specified to pack edit form into, if not specified
 *         the main window workspace is used to pack item composite into</li>
 *     <li><b>itemUsecase</b> if mode is CONTAINER it must be specified to run this item edit usecase</li>
 *     <li><b>filter</b> filter DTO to apply to list view</li>
 *     <li>b<b>itemControllerClass</b> is CommonItemController subclass for item handling</li>
 *     <li>b<b>itemDialogTitle</b> is item dialog title</li>
 * </ul></p>
 *
 * @author pavlov
 * @since 1.2
 *
 */
abstract public class SimpleListController 
	extends CommonListController
	implements IListEditController {
	
	public SimpleListController(ICompositeController parentController,
			Class<? extends CommonObject> dataClass, CompositeParams compositeParams) {
		super(parentController, dataClass, compositeParams);
	}

	public SimpleListController(ICompositeController parentController,
			Class<? extends CommonObject> dataClass, Composite container, CompositeParams compositeParams) { 
		super(parentController, dataClass, container, compositeParams);
	}
	
	/**
	 * <p>Handler for item double click event.</p>
	 *
	 * <p>This handler works with data item got by getActiveData(),
	 * note the @data parameter is not the same as getActiveData(),
	 * the method can be overridden in child classes to work with other
	 * data item then selected in list, but @data always contains selected
	 * data item DTO.</p>
	 * 
	 * @param data
	 */
	public void handleItemDoubleClicked(CommonDTO data) {
		if (data != null) {
			if (params().get("itemEditMode") != CommonItemController.EditMode.CONTAINER) {
				Object dlg = openEditDialog((EditorDTO)getActiveData());
				if (dlg != null) {
					try {
						// Re-read data into list
						handleDataQuery();
					} catch (DTOException ex) {
						handleError("Unable to refresh list", ex);
					}
				}
			} else {
				CommonItemController ctrl = openEdit(getParentController(), (EditorDTO)data);
				ctrl.setEditMode((CommonItemController.EditMode)params().get("itemEditMode"));
			}
		}
	}
	
	public void addDragSupport(int operations, Transfer[] transferTypes, DragSourceListener listener) {
		IDragDropDecorator tbl = (IDragDropDecorator)((SimpleListComposite)getComposite()).getTable();
		if (tbl != null)
			tbl.addDragSupport(operations, transferTypes, listener);
	}
	
	@Override
	public void handleCreate() {
		if (params().get("itemEditMode") != CommonItemController.EditMode.CONTAINER) {
			if (openEditDialog(null) != null) {	
				try {
					handleDataQuery();
				} catch (DTOException ex) {
					handleError("Can not refresh list", ex);
				}
			}
		} else {
			openEdit(getParentController(), null);
		}
	}
	
	@Override
	public void handleDelete() {
		String id = "";
		// Только если есть выбранный элемент, есть активные данные
		if (getActiveData() != null) {
			try {
				id = getActiveData().
						getDataField("mId", "getId", "setId").toString();
				CommonObject obj = getSession().persistent().open(getDataClass(), id);
				obj.delete();
				obj.save();
				logger().debug("Item deleted, ID=", id);
			} catch (DTOException ex) {
				handleError("DTO exception on delete item", ex);
			} catch (OpenException ex) {
				handleError("Open exception on delete item", ex);
			} catch (SaveException ex) {
				handleError("Save exception on delete item", ex);
			}

			// Обновить список, перечитать данные
			try {
				handleDataQuery();
			} catch (DTOException ex) {
				handleError("Can not refresh list", ex);
			}			
		}
	}

	@Override
	public void handleEdit() {
		handleItemDoubleClicked(getActiveData());
	}

	@Override
	protected CommonComposite createComposite(CompositeParams compositeParams) {
		// Add FilterDTO object to composite params
		params().add("filter", getFilter());
		
		@SuppressWarnings("unchecked")
		Class<? extends CommonListComposite> listCompositeClass = 
				(Class<? extends CommonListComposite>)params().get("listComposite");
		if (listCompositeClass != null) {
			SimpleListComposite composite = null;
			try {
				Constructor<?> constr = listCompositeClass.getConstructor(ApplicationSession.class,
						Composite.class, int.class, CompositeParams.class);
				composite = (SimpleListComposite) constr.newInstance(
						getSession(), getContainer(), SWT.NONE, params());
			} catch (Exception ex) {
				logger().error("Не удалось создать окно композита для контроллера", ex);
			}
			return composite;
		} else {
			return new SimpleListComposite(getSession(), getContainer(), SWT.NONE, params());
		}
	}

	@Override
	public void handleRecover() throws DTOException {
		
	}

	@Override
	protected void doAfterCreateComposite() {
		getComposite().addControllerListener(createControllerListener());
		((SimpleListComposite)getComposite()).createTableColumns();
		
		try {
			handleDataQuery();
		} catch (DTOException ex) {
			handleError("Can not refresh list: " + ex.getMessage(), ex);
		}
	}
	
	/**
	 * Opens item edit dialog and returns dialog result.
	 * 
	 * @param data
	 * @return
	 */
	protected Object openEditDialog(EditorDTO data) {
		CommonItemDialog dlg = new CommonItemDialog(getSession(), getComposite(), this, params());		
		return dlg.open(data);
	}

	/**
	 * Open item edit or creation as subconroller.
	 * 
	 * @param parentController
	 * @param data
	 * @return
	 */
	private CommonItemController openEdit(ICompositeController parentController, EditorDTO data) {
		Composite container = (Composite) params().get("itemEditContainer");
		String itemUsecaseBundle = (String) params().get("itemUsecase");
		
		CommonItemController ctrl;		
		if (itemUsecaseBundle != null) {
			logger().info("Running usecase {} to edit item", itemUsecaseBundle);
			
			/*
			 * Fill usecase params struct with ID of data item to edit.
			 */
			HashMap<String, Object> params = new HashMap<String, Object>();
			if (data != null) {
				try {
					params.put("id", data.getId());
				} catch (DTOException ex) {}
			} else {
				params.put("id", "");
			}
			
			/*
			 * Run usecase to edit item
			 */
			ctrl = (CommonItemController)((CompositeController)parentController).
					handleRunUsecase(itemUsecaseBundle, params, true);
		} else {
			logger().info("Not running usecase to edit item but create item controller");
			ctrl = createItemController(parentController, container, new CompositeParams());

			try {
				if (parentController != null)
					parentController.addSubController(ctrl);
			} catch (InitException ex) {
				ctrl.handleError("Ошибка инициализации контроллера.", ex);
				return null;
			}

			// Initialize controller
			try {
				ctrl.init();
			} catch (InitException ex1) {
				ctrl.handleError("Ошибка инициализации контроллера.", ex1);
				return null;
			}
		
			// Check composite is created right
			if (ctrl.getComposite() == null) {			
				ctrl.uninit();
				ctrl.handleError("Композит для отображения в диалоговом окне не создан!", null);
				return null;
			}
		
			ctrl.getComposite().layout(true, true);

			// Create or open data
			if (data == null) {
				// Создание нового элемента
				try {
					ctrl.create();
				} catch (DTOException ex) {
					ctrl.handleError("Ошибка создания нового элемента.", ex);
					ctrl.uninit();
					return null;
				}
			} else {
				// Открыть на редактирование существующий элемент
				try {
					String id = data.getDataField("id", "getId", "setId").toString();
					ctrl.getSession().logger().debug("Открытие окна редакитрования для элмента с ID: {}", id);
					ctrl.openById(id);
				} catch (NumberFormatException ex) {
					handleError("Неверный формат ID.", ex);
					ctrl.uninit();
					return null;
				} catch (OpenException ex) {
					handleError("Ошибка открытия элемента.", ex);				
					ctrl.uninit();
					return null;
				} catch (DTOException ex) {
					handleError("Ошибка доступа в DTO.", ex);
					ctrl.uninit();
					return null;
				}
			}
		}

		return ctrl;		
	}
	
	@Override
	public CommonControllerListener createControllerListener() {
		return new SimpleListControllerListener(this);
	}
	
	@Override
	protected void doBeforeInit() throws InitException {}
	
	@Override
	protected void doAfterInit() throws InitException {}
	
	@Override
	public void handleDataQueryExecuted() throws DTOException {
		((CommonListComposite)getComposite()).disableToolbar();

		setToolEnabled("create", true);

		((CommonListComposite)getComposite()).refresh();		
	}

	@Override
	public void processUsecaseParams() {}

	/**
	 * Column resize event handler.
	 * 
	 * @param column
	 */
	protected void onTableColumnResized(TableColumn column) {}
	
	/**
	 * Column creation event handler.
	 * 
	 * @param column
	 */
	protected void onTableColumnAdded(TableColumn column, String columnField, 
			String columnFieldGetter, String columnFieldSetter, Class<?> columnFieldType) {}
	
}
