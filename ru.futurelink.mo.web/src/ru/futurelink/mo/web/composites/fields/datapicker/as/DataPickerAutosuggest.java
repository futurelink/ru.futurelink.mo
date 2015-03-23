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

package ru.futurelink.mo.web.composites.fields.datapicker.as;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.IDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.CommonItemComposite;
import ru.futurelink.mo.web.composites.CommonPopup;
import ru.futurelink.mo.web.composites.fields.datapicker.DataPickerController;
import ru.futurelink.mo.web.composites.fields.datapicker.DataPicker;
import ru.futurelink.mo.web.controller.CompositeParams;
import ru.futurelink.mo.web.controller.iface.ICompositeController;
import ru.futurelink.mo.web.controller.iface.IListController;
import ru.futurelink.mo.web.exceptions.InitException;

/**
 * Data picker field with autosuggest capability.
 * 
 * Note that parent controller should be passed to this field
 * instance after it is initailized. The field has no its own
 * controller but it is needed to autosuggest list controller creation.
 * So we need to call setParentController on each data picker field with
 * autosuggestion in parent controller doAfterInit metehod.
 * 
 * @author pavlov
 *
 */
public class DataPickerAutosuggest extends DataPicker {

	private CommonPopup autoSuggestPopup;
	private int			autoSuggestHeight = 150;
	private boolean		modifiedProgrammaticaly;
	
	private Class <? extends AutosuggestController>	
									autoSuggestControllerClass;
	private AutosuggestController	autoSuggestController;
	private AutosuggestControllerListener 
									autoSuggestControllerListener;
	
	/**
	 * @param session
	 * @param parent
	 * @param style
	 * @param params
	 * @param dto
	 * @param pickerController
	 */
	public DataPickerAutosuggest(ApplicationSession session,
			CommonComposite parent, 
			int style, 
			CompositeParams params, 
			CommonItemComposite dataComposite,
			Class<? extends DataPickerController> pickerController,
			Class<? extends AutosuggestController> autoSuggestControllerClass) {
		super(session, parent, style, params, dataComposite, pickerController);
		
		this.autoSuggestControllerClass = autoSuggestControllerClass;
		createASControllerListener();
	}

	public DataPickerAutosuggest(ApplicationSession session, 
			CommonComposite parent, 
			int style, 
			CompositeParams params,
			CommonDTO dto,
			Class<? extends DataPickerController> pickerController,
			Class<? extends AutosuggestController> autoSuggestControllerClass) {
		super(session, parent, style, params, dto, pickerController);
		
		this.autoSuggestControllerClass = autoSuggestControllerClass;
		createASControllerListener();
	}
	
	private void createASControllerListener() {
		if (autoSuggestControllerListener == null)
			autoSuggestControllerListener = new AutosuggestControllerListener() {	
				@Override
				public void sendError(String errorText, Exception exception) {
				
				}
			
				@Override
				public void setSelectedDTO(IDTO data) throws DTOException {
					DataPickerAutosuggest.this.setSelectedDTO(data);

					// Close popup after selection
					autoSuggestPopup.close();
				}

				@Override
				public void cancelAutosuggest() {
					autoSuggestPopup.close();					
				}
			};
	}
	
	protected void createControls(int style) {
		super.createControls(style);

		mEdit.setEditable(true);

		// Enable autosuggest editing handler
		autoSuggestPopup = new CommonPopup(getSession(), parent.getShell(), SWT.BORDER);

		mEdit.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void modifyText(ModifyEvent arg0) {
				if ((getParentController() != null) && (autoSuggestControllerClass != null)) {
					handleTextModification();
				} else {
					// Parent controller is not set but autosuggest is enabled
					System.out.println("No parent controller passed of autosuggest controller class is null. "
							+ "Autosuggest will not work!");
				}
			}
		});

		mEdit.addFocusListener(new FocusListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void focusLost(FocusEvent arg0) {
				if (mEdit.getText().equals(""))
					mEdit.setText(HINTSTRING);
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				if (mEdit.getText().equals(HINTSTRING))
					mEdit.setText("");
			}
		});	
	}

	private void handleTextModification() {
		// Show popup selection list if (1) entered text length is more than 3 chars
		// (2) list is not shown, (3) text modification is not programmatic,
		// (4) control is currently focused
		if (!modifiedProgrammaticaly && !mEdit.getText().equals(HINTSTRING)) {
			if ((mEdit.getText().length() >= 3) && (autoSuggestController == null) &&
				mEdit.isFocusControl()) {

				autoSuggestPopup.open();							
				autoSuggestPopup.getShell().addDisposeListener(new DisposeListener() {
					private static final long serialVersionUID = 1L;

					@Override
					public void widgetDisposed(DisposeEvent arg0) {
						try {
							refresh();
						} catch (DTOException ex) {
							autoSuggestController.handleError("Autosuggest cancel error...", ex);
						}
						hidePopup();									
					}
				});

				// Prepare data for list selection
				if (getPrepareListener() != null)
					getPrepareListener().prepare();
				
				// Create autosuggest controller with composite packed into
				// popup shell.
				autoSuggestController = createAutoSuggestController();
				autoSuggestController.addControllerListener(autoSuggestControllerListener);
				try {
					autoSuggestController.init();
				} catch (InitException ex) {
					autoSuggestController.handleError("Error in autosuggesting...", ex);
					autoSuggestController = null;
					return;
				}

				// Position popup shell under the control
				Point coords = control.toDisplay(-6, -6);
				Rectangle bounds = control.getBounds();
				autoSuggestPopup.attachComposite(autoSuggestController.getComposite());
				autoSuggestPopup.moveTo(new Point(coords.x-1, coords.y-1));
				autoSuggestPopup.setSize(bounds.width+12, autoSuggestHeight);				

				// Focus on search composite
				autoSuggestController.focusOnEdit();
			} else {
				if ((mEdit.getText().length() < 3) && 
					(autoSuggestController != null))
					hidePopup();							
			}

			// If there is a controller - query data on it
			if (autoSuggestController != null) {
				try {
					autoSuggestController.setQuery(mEdit.getText(), mEdit.getCaretPosition());
					((IListController)autoSuggestController).handleDataQuery();
				} catch (DTOException ex) {
					autoSuggestController.handleError("Error in autosuggesting...", ex);
					return;
				}
			}
		}
	}
	
	private void hidePopup() {
		// Hide popup selection list
		if (autoSuggestController != null) {
			if((autoSuggestPopup.getShell() != null) && 
				!autoSuggestPopup.getShell().isDisposed())
				autoSuggestPopup.close();

			// Clear autosuggest composite
			if (autoSuggestController != null)
				autoSuggestController.uninit();
			autoSuggestController = null;
		}
	}

	@Override
	public void refresh() throws DTOException { 
		modifiedProgrammaticaly = true;
		super.refresh();
		modifiedProgrammaticaly = false;
	}

	private AutosuggestController createAutoSuggestController() {
		try {
			Constructor<? extends AutosuggestController> autoSuggestCons = 
				autoSuggestControllerClass.getConstructor(
					ICompositeController.class, Class.class, 
					Composite.class, CompositeParams.class);
			
			return autoSuggestCons.newInstance(
					getParentController(), 
					getDataClass(), 
					autoSuggestPopup.getShell(), 
					getParams());

		} catch (NoSuchMethodException | SecurityException | InstantiationException | 
				IllegalAccessException | IllegalArgumentException | 
				InvocationTargetException ex) {
			ex.printStackTrace();
		}

		return null;
	}
}
