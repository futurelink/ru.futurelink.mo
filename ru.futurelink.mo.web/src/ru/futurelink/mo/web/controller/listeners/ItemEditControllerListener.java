/**
 * 
 */
package ru.futurelink.mo.web.controller.listeners;

import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.exceptions.SaveException;
import ru.futurelink.mo.web.composites.fields.IField;
import ru.futurelink.mo.web.composites.fields.datapicker.DataPicker;
import ru.futurelink.mo.web.controller.CommonItemController;
import ru.futurelink.mo.web.controller.CommonItemEditControllerListener;

/**
 * @author pavlov
 *
 */
public class ItemEditControllerListener implements
		CommonItemEditControllerListener {

	private CommonItemController mController;
	
	public ItemEditControllerListener(CommonItemController contorller) {
		mController = contorller;
	}

	@Override
	public void sendError(String errorText, Exception exception) {
		mController.handleError(errorText, exception);
	}
	
	@Override
	public void dataChanged(IField editor) throws DTOException {
		handleSaveButton();
	}

	@Override
	public void dataChangeFinished(IField editor) throws DTOException {
		handleSaveButton();
	}
	
	@Override
	public void openDataPickerDialog(DataPicker picker) {
		mController.handleOpenDataPickerDialog(picker);
	}
				
	@Override
	public void saveButtonClicked() {
		try {
			mController.save();
			mController.close();					
		} catch (SaveException | DTOException e) {
			mController.handleError("Ошибка сохранения карты!", e);
			return;
		}				
	}			

	@Override
	public void cancelButtonClicked() {
		mController.close();
	}
	
	private void handleSaveButton()  throws DTOException {
		// Если не менялось ничего ни в этом контроллере, ни в связанных - то
		// кнопка недоступна.
		if (((mController.getDataChanged() != null) && 
				!mController.getDataChanged().isEmpty()) || mController.getRelatedDataChanged()) {
			mController.setSaveButtonEnabled(true);
		} else {
			mController.setSaveButtonEnabled(false);
		}
	}
}
