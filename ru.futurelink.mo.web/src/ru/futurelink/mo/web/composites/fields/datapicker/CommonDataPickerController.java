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

import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.CommonDTOList;
import ru.futurelink.mo.orm.dto.EditorDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
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
abstract public class CommonDataPickerController 
	extends CommonListController
	implements IListEditController {

	private EditorDTO				mActiveData;

	public CommonDataPickerController(CompositeController parentController,
			Class<? extends CommonObject> dataClass, Composite container,
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
			logger().info("Запущено создание элмента из датапикера");
			logger().info("Класс контроллера: {}", controllerClass.getSimpleName());
			
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
		mActiveData = (EditorDTO) ((CommonListComposite)getComposite()).getActiveData();		
		Class<?> controllerClass = (Class<?>) params().get("itemControllerClass");
		if ((controllerClass != null) && (getActiveData() != null)) {
			logger().info("Запущен просмотр элмента из датапикера");
			logger().info("Класс контроллера: {}", controllerClass.getSimpleName());
			
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
	 * По двойному клику - выбираем элемент.
	 * 
	 * @param data
	 */
	public void handleItemDoubleClicked(CommonDTO data) {
		handleOk();
	}

	/**
	 * Выбираем элемент и диспозим оконошко.
	 */
	public void handleOk() {
		try {
			mActiveData = (EditorDTO) ((CommonListComposite)getComposite()).getActiveData();
			if (mActiveData != null) {
				logger().debug("dataPicker complete data: {}",
						mActiveData.getDataField("id", "getId", "setId"));

				// Закрыть окно
				getComposite().dispose();				
			}
		} catch (DTOException ex) {
			handleError("Ошибка выбора элемента", ex);
		}
	}

	@Override
	protected void doAfterCreateComposite() {
		getComposite().addControllerListener(createControllerListener());
		
		try {
			handleDataQuery();
		} catch (DTOException ex) {
			handleError("Ошибка получения списка выбора.", ex);
		}
	}

	/**
	 * Получить выбранный элемент.
	 * 
	 * @return DTO выбранного элемента
	 */
	final public EditorDTO getActiveData() {
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
			public void itemSelected(CommonDTO data) {
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
			public void itemDoubleClicked(CommonDTO data) {
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
			public CommonDTOList<? extends CommonDTO> getControllerDTO() throws DTOException {
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
		// Запрещаем все кнопки и контролы на тулбаре (по-умолчанию).
		if (getComposite() != null) {
			((CommonDataComposite)getComposite()).disableToolbar();
			((CommonListComposite)getComposite()).refresh();

			// Если элемент остается выбранным, то нужно
			// разрешить и редактирование и выбор.
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
