/**
 * 
 */
package ru.futurelink.mo.web.controller;

import org.eclipse.swt.widgets.TableColumn;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.CommonDTOList;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.exceptions.InitException;

/**
 * @author pavlov
 *
 */
public class SimpleListControllerListener implements
		CommonListControllerListener, CommonTableControllerListener {

	private SimpleListController mListController;
	
	public SimpleListControllerListener(SimpleListController controller) {
		mListController = controller;
	}
		
	@Override
	public void sendError(String errorText, Exception exception) {
		mListController.handleError(errorText, exception);

		// Передаем событие выще - контроллеру-обработчику
		if (mListController.getControllerListener() != null) {
			mListController.getControllerListener().sendError(errorText, exception);
		}
	}

	@Override
	public void create() throws DTOException, InitException {
		mListController.handleCreate();	// Передаем команду на создание контроллеру

		// Передаем событие выще - контроллеру-обработчику
		if (mListController.getControllerListener() != null) {
			((CommonListControllerListener)mListController.getControllerListener()).create();
		}
	}

	@Override
	public void edit() throws DTOException {
		mListController.handleEdit(); // Передаем команду на редактирование контроллеру

		// Передаем событие выще - контроллеру-обработчику
		if (mListController.getControllerListener() != null) {
			((CommonListControllerListener)mListController.getControllerListener()).edit();
		}
	}

	@Override
	public void delete() throws DTOException {
		mListController.handleDelete(); // Передаем команду на удаление контроллеру

		// Передаем событие выще - контроллеру-обработчику
		if (mListController.getControllerListener() != null) {
			((CommonListControllerListener)mListController.getControllerListener()).delete();
		}
	}

	@Override
	public void itemSelected(CommonDTO data) {
		if (data != null) {
			mListController.setToolEnabled("edit", true);
			mListController.setToolEnabled("delete", true);
		} else {
			mListController.setToolEnabled("edit", false);
			mListController.setToolEnabled("delete", false);
		}

		// Передаем событие выще - контроллеру-обработчику
		if (mListController.getControllerListener() != null) {
			((CommonListControllerListener)mListController.getControllerListener()).itemSelected(data);
		}
	}

	@Override
	public void itemDoubleClicked(CommonDTO data) {
		mListController.handleItemDoubleClicked(data);

		// Передаем событие выще - контроллеру-обработчику
		if (mListController.getControllerListener() != null) {
			((CommonListControllerListener)mListController.getControllerListener()).itemDoubleClicked(data);
		}
	}

	/**
	 * Обработка изменения размера колонки.
	 * 
	 * @param column
	 */
	@Override
	public void onColumnResized(TableColumn column) {
		mListController.onTableColumnResized(column);
	}

	@Override
	public void filterChanged() {}

	@Override
	public CommonDTOList<? extends CommonDTO> getControllerDTO() throws DTOException {
		return mListController.getDTO();
	}

	/**
	 * Обработка добавления колонки в таблицу.
	 * 
	 * @param column
	 */
	@Override
	public void onColumnAdded(TableColumn column, String filterField,
			String filterFieldGetter, String filterFieldSetter, Class<?> filterFieldType) {
		mListController.onTableColumnAdded(column, filterField, filterFieldGetter, filterFieldSetter, filterFieldType);
	}

	/**
	 * Refresh list.
	 * 
	 * @param refreshSubcontrollers
	 * @throws DTOException
	 */
	public void refresh(boolean refreshSubcontrollers) throws DTOException {
		mListController.refresh(refreshSubcontrollers);
	}

}
