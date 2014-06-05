/**
 * 
 */
package ru.futurelink.mo.web.composites.dialogs;

import org.eclipse.swt.SWT;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.exceptions.OpenException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.controller.CommonItemController;
import ru.futurelink.mo.web.controller.CommonListController;
import ru.futurelink.mo.web.controller.CompositeParams;
import ru.futurelink.mo.web.exceptions.InitException;

/**
 * @author pavlov
 *
 */
public class CommonItemDialog {	
	private CommonItemController	mController = null;
	private CommonListController	mListController = null;
	private CommonDialog			mDialog = null;
	private String					mItemDialogTitle = "";
	private Integer 				mItemDialogSize = null;

	/**
	 * @param session
	 * @param parent
	 * @param style
	 */
	public CommonItemDialog(
			ApplicationSession session, 
			CommonComposite parent, 
			CommonListController controller, 
			CompositeParams params) 
	{
		mItemDialogSize = (Integer) params.get("itemDialogSize");
		mItemDialogTitle = (String) params.get("itemDialogTitle");

		// Смотрим, какой стиль диалога будет и вычисляем стиль
		int style = 0;
		if ((mItemDialogSize == null) || (mItemDialogSize != CommonDialog.FIXED)) {
			style = SWT.RESIZE | SWT.MAX;
		}

		mDialog = new CommonDialog(session, parent.getShell(), style);
		if (params.get("itemDialogHeight") != null) mDialog.setFixedHeight((Integer) params.get("itemDialogHeight"));
		if (params.get("itemDialogWidth") != null) mDialog.setFixedWidth((Integer) params.get("itemDialogWidth"));

		mListController = controller;
	}
	
	public Object open(CommonDTO data) {
		mController = mListController.createItemController(mListController, mDialog.getShell());
		
		try {
			mController.init();
		} catch (InitException ex1) {
			mController.handleError("Ошибка инициализации контроллера.", ex1);
			return -1;
		}
		
		if (mController.getComposite() == null) {			
			mController.uninit();
			mController.handleError("Композит для отображения в диалоговом окне не создан!", null);
			return -1;
		}

		// Установим тайтл диалогового окна
		if (mItemDialogTitle != null)
			mDialog.setText(mController.getComposite().getLocaleString(mItemDialogTitle));
		
		mDialog.attachComposite(mController.getComposite());
		if (mItemDialogSize != null) {
			mDialog.setSize(Integer.valueOf(mItemDialogSize.toString()));
		} else {
			mDialog.setSize(CommonDialog.MEDIUM);
		}

		if (data == null) {
			// Создание нового элемента
			try {
				mController.create();
			} catch (DTOException ex) {
				mController.handleError("Ошибка создания нового элемента.", ex);
				mDialog = null;
				mController.uninit();
				return 0;
			}
		} else {
			// Открыть на редактирование существующий элемент
			try {
				String id = data.getDataField("id", "getId", "setId").toString();
				mController.getSession().logger().debug("Открытие окна редакитрования для элмента с ID: {}", id);
				mController.openById(id);
			} catch (NumberFormatException ex) {
				mController.handleError("Неверный формат ID.", ex);
				mDialog = null;
				mController.uninit();
				return 0;
			} catch (OpenException ex) {
				mController.handleError("Ошибка открытия элемента.", ex);
				mDialog = null;
				mController.uninit();
				return 0;
			} catch (DTOException ex) {
				mController.handleError("Ошибка доступа в DTO.", ex);
				mDialog = null;
				mController.uninit();
				return 0;
			}
		}
		
		mDialog.open();

		// Чистим контроллер того окна, которое было создано
		mController.uninit();
		
		return mDialog.getResult();
	}

}
