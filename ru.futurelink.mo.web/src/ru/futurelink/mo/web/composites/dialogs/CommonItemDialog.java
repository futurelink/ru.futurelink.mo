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
		mController = mListController.createItemController(mListController, mDialog.getShell(), new CompositeParams());
		
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

		// Set dialog window title
		if (mItemDialogTitle != null)
			mDialog.setText(mController.getComposite().getLocaleString(mItemDialogTitle));
		
		mDialog.attachComposite(mController.getComposite());
		if (mItemDialogSize != null) {
			mDialog.setSize(Integer.valueOf(mItemDialogSize.toString()));
		} else {
			mDialog.setSize(CommonDialog.MEDIUM);
		}

		if (data == null) {
			// Create new data item
			try {
				mController.create();
			} catch (DTOException ex) {
				mController.handleError("Error creating data item.", ex);
				mDialog = null;
				mController.uninit();
				return 0;
			}
		} else {
			// Open and edit existing data
			boolean err = false;
			try {
				String id = data.getDataField("id", "getId", "setId").toString();
				if (id == null) {
					mController.handleError("ID is null. It's impossible but real.", null);
					err = true;
				} 
				mController.getSession().logger().debug("Opening edit dialog for ID: {}", id);
				mController.openById(id);
			} catch (NumberFormatException ex) {
				mController.handleError("ID invalid format exception.", ex);
				err = true;
			} catch (OpenException ex) {
				mController.handleError("Data element open error.", ex);
				err = true;
			} catch (DTOException ex) {
				mController.handleError("DTO access exception.", ex);
				err = true;
			}
		
			// If there was an error, don't open dialog and return
			if (err) {
				mDialog = null;
				mController.uninit();
				return 0;
			}
		}
				
		mDialog.open();

		// Clear and uninit controller
		mController.uninit();
		
		return mDialog.getResult();
	}

}
