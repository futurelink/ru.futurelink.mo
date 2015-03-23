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

import java.util.AbstractCollection;
import java.util.Iterator;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

import ru.futurelink.mo.orm.dto.CommonDTOList;
import ru.futurelink.mo.orm.dto.EditorDTO;
import ru.futurelink.mo.orm.dto.IDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.iface.ICommonObject;
import ru.futurelink.mo.web.composites.CommonDataComposite;
import ru.futurelink.mo.web.composites.CommonListComposite;
import ru.futurelink.mo.web.composites.dialogs.CommonItemDialog;
import ru.futurelink.mo.web.controller.CommonControllerListener;
import ru.futurelink.mo.web.controller.CompositeController;
import ru.futurelink.mo.web.controller.CompositeParams;
import ru.futurelink.mo.web.controller.CommonListController;
import ru.futurelink.mo.web.controller.iface.IListEditController;
import ru.futurelink.mo.web.exceptions.InitException;

/**
 * Контроллер списка используемый для выбора элемента данных в контроле DataPicker.
 * По логике DataPicker - это обрезаный до просмотра данных виджет списка, используемый
 * только для выбора нужного элемента.
 * 
 * @author Futurelink
 *
 */
abstract public class DataPickerController 
	extends CommonListController
	implements IListEditController {

	private IDTO mActiveData;

	public DataPickerController(CompositeController parentController,
			Class<? extends ICommonObject> dataClass, Composite container,
			CompositeParams compositeParams) {
		super(parentController, dataClass, container, compositeParams);
	}

	public static String join(AbstractCollection<String> s, String delimiter) {
		  if (s == null || s.isEmpty()) return "";
		  Iterator<String> iter = s.iterator();
		  StringBuilder builder = new StringBuilder(iter.next());
		  while( iter.hasNext() ) {
			  builder.append(delimiter).append(iter.next());
		  }
		  return builder.toString();
	  }
	
	@Override
	public void handleCreate() throws DTOException {	
		Class<?> controllerClass = (Class<?>) params().get("itemControllerClass");
		if (controllerClass != null) {
			logger().info("Creation from picker executed");
			logger().info("Item controller class is {}", controllerClass.getSimpleName());
			
			CompositeParams itemDialogParams = (params().get("itemDialogParams") != null) ? 
					(CompositeParams) params().get("itemDialogParams") : new CompositeParams();
			if (new CommonItemDialog(getSession(), getComposite(), this, itemDialogParams).open(null) != null) {
				handleDataQuery();
			}
		}
	}

	@Override
	public void handleDelete() throws DTOException {
		handleDataQuery();
	}

	@Override
	public void handleEdit() throws DTOException {
		mActiveData = ((CommonListComposite)getComposite()).getActiveData();		
		Class<?> controllerClass = (Class<?>) params().get("itemControllerClass");
		if ((controllerClass != null) && (getActiveData() != null)) {
			logger().info("View from picker executed");
			logger().info("Item controller class is {}", controllerClass.getSimpleName());
			
			CompositeParams itemDialogParams = (params().get("itemDialogParams") != null) ? 
					(CompositeParams) params().get("itemDialogParams") : new CompositeParams();
			if (new CommonItemDialog(getSession(), getComposite(), this, itemDialogParams).open(getActiveData()) != null) {
				handleDataQuery();
			}
		}
	}

	@Override
	public void handleRecover() throws DTOException {
		
	}

	/**
	 * Double click handler behaves as OK button
	 * 
	 * @param data
	 */
	public void handleItemDoubleClicked(IDTO data) {
		handleOk();
	}

	/**
	 * Select item and dispose window
	 */
	public void handleOk() {
		try {
			mActiveData = (EditorDTO) ((CommonListComposite)getComposite()).getActiveData();
			if (mActiveData != null) {
				logger().debug("dataPicker complete data: {}",
						mActiveData.getDataField("id", "getId", "setId"));
			}
		} catch (DTOException ex) {
			handleError("Element selection error", ex);
		} finally {
			getComposite().dispose();
		}
	}

	@Override
	protected void doAfterCreateComposite() {
		getComposite().addControllerListener(createControllerListener());
		
		try {
			handleDataQuery();
		} catch (DTOException ex) {
			getComposite().dispose();

			handleError("List selection error", ex);
		}
	}

	/**
	 * Get selected data item
	 * 
	 * @return selected item's DTO 
	 */
	final public IDTO getActiveData() {
		return mActiveData;
	}

	@Override
	public void init() throws InitException {
		super.init();
		setToolEnabled("close", true);
		setToolEnabled("save", false);
		setToolEnabled("edit", false);
	}
	
	@Override
	public CommonControllerListener createControllerListener() {
		return new DataPickerControllerListener() {			
			@Override
			public void sendError(String errorText, Exception exception) {
				handleError(errorText, exception);
			}

			@Override
			public void create() throws DTOException {
				handleCreate();
			}

			@Override
			public void edit() throws DTOException {
				handleEdit();
			}

			@Override
			public void delete() {}

			@Override
			public void itemSelected(IDTO data) {
				try {
					if (data != null) {
						setToolEnabled("save", true);
						setToolEnabled("edit", true);
						logger().debug("dataPicker itemSelected: {}", data.getDataField("id", "getId", "setId"));
					} else {
						setToolEnabled("save", false);
						setToolEnabled("edit", false);
					}
				} catch (DTOException ex) {
					ex.printStackTrace();
				}				
			}

			@Override
			public void itemDoubleClicked(IDTO data) {
				if (data != null) {
					handleItemDoubleClicked(data);
				}
			}

			@Override
			public void ok() {
				handleOk();
			}

			@Override
			public void cancel() {
				getComposite().dispose();
			}

			@Override
			public void filterChanged() {}

			@Override
			public CommonDTOList<? extends IDTO> getControllerDTO() throws DTOException {
				return getDTO();
			}

			@Override
			public void onColumnResized(TableColumn column) {}

			@Override
			public void onColumnAdded(TableColumn column,
					String filterField, 
					String filterFieldGetter,
					String filterFieldSetter,
					Class<?> filterFieldType) {}
		};
	}

	@Override
	protected void doBeforeInit() {}

	@Override
	protected void doAfterInit() {}

	@Override
	public void refresh(boolean refreshSubcontrollers) throws DTOException {
		super.refresh(refreshSubcontrollers);

		((CommonListComposite)getComposite()).refresh();
	}

	@Override
	public void handleDataQueryExecuted() throws DTOException {	
		// Disable all buttons on toolbar by default
		if (getComposite() != null) {
			((CommonDataComposite)getComposite()).disableToolbar();
			((CommonListComposite)getComposite()).refresh();

			// If there is an active record
			// we enable saving and editing
			mActiveData = (EditorDTO) ((CommonListComposite)getComposite()).getActiveData();
			if (mActiveData != null) {
				setToolEnabled("save", true);
				setToolEnabled("edit", true);
			}
			
			setToolEnabled("close", true);
			setToolEnabled("create", true);
		}
	}

}
