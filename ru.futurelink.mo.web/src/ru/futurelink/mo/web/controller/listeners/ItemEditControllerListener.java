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

package ru.futurelink.mo.web.controller.listeners;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.JavaScriptExecutor;

import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.exceptions.SaveException;
import ru.futurelink.mo.web.composites.fields.IField;
import ru.futurelink.mo.web.controller.CommonItemController;
import ru.futurelink.mo.web.controller.CommonItemController.EditMode;
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
	public void saveButtonClicked() {
		try {
			mController.save();
			mController.saveCommit();
			mController.close();					
		} catch (SaveException | DTOException e) {
			mController.handleError("Save or DTO operation exception", e);
			return;
		}
	}			

	@Override
	public void cancelButtonClicked() {
		// If edit is executed in dialog mode we close that dialog,
		// otherwise simply go back in browser.
		// This is needed to handle browser history correctly.
		if (mController.getEditMode() != EditMode.CONTAINER) {
			mController.close();
		} else {
			JavaScriptExecutor executor = RWT.getClient().getService( JavaScriptExecutor.class );
			executor.execute( "window.history.back();");
		}
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
