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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.orm.dto.CommonDTOList;
import ru.futurelink.mo.orm.dto.IDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.iface.ICommonObject;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.CommonListComposite;
import ru.futurelink.mo.web.controller.CommonControllerListener;
import ru.futurelink.mo.web.controller.CommonListController;
import ru.futurelink.mo.web.controller.CompositeParams;
import ru.futurelink.mo.web.controller.iface.ICompositeController;
import ru.futurelink.mo.web.exceptions.InitException;

/**
 * @author pavlov
 *
 */
public abstract class AutosuggestController extends CommonListController {
	
	private  String currentQuery;
	
	/**
	 * @param parentController
	 * @param dataClass
	 * @param container
	 * @param compositeParams
	 */
	public AutosuggestController(
			ICompositeController parentController,
			Class<? extends ICommonObject> dataClass, Composite container,
			CompositeParams compositeParams) {
		super(parentController, dataClass, container, compositeParams);
	}

	public void focusOnEdit() {
		((AutosuggestComposite)getComposite()).focusOnEdit();
	}

	public void setQuery(String query, int caretPosition) {
		currentQuery = query;
		((AutosuggestComposite)getComposite()).setText(query, caretPosition);
	}

	public String getQuery() {
		 return currentQuery;
	}

	@Override
	protected CommonComposite createComposite(CompositeParams params) {
		AutosuggestComposite c = new AutosuggestComposite(
				getSession(), getContainer(), SWT.BORDER, params);
		c.addControllerListener(createControllerListener());
		return c;
	}

	@Override
	public void handleDataQueryExecuted() throws DTOException {
		((CommonListComposite)getComposite()).refresh();
	}

	@Override
	protected void doBeforeCreateComposite() {}

	@Override
	protected void doAfterCreateComposite() {}

	@Override
	protected void doBeforeInit() throws InitException {}

	@Override
	protected void doAfterInit() throws InitException {	
		try {
			((CommonListComposite)getComposite()).setInput(getDTO());
		} catch (DTOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public CommonControllerListener createControllerListener() {
		return new AutosuggestListControllerListener() {
			
			@Override
			public CommonDTOList<? extends IDTO> getControllerDTO() throws DTOException {
				return getDTO();
			}

			@Override
			public void searchTextModified(String text) {
				currentQuery = text;
				try {
					refresh(true);
				} catch (DTOException ex) {
					// Search unsuccessful
				}
			}

			@Override
			public void sendError(String errorText, Exception exception) {
				handleError(errorText, exception);
			}

			@Override
			public void itemSelected(IDTO data) {
				try {
					if (getControllerListener() != null)
						((AutosuggestControllerListener)getControllerListener()).setSelectedDTO(data);
				} catch (DTOException ex) {
					// Selection is invalid
				}
			}

			@Override
			public void cancelAutosuggest() {
				if (getControllerListener() != null)
					((AutosuggestControllerListener)getControllerListener()).cancelAutosuggest();
			}

			@Override
			public void itemDoubleClicked(IDTO data) {}
			
			@Override
			public void filterChanged() {}
			
			@Override
			public void edit() throws DTOException {}
			
			@Override
			public void delete() throws DTOException {}
			
			@Override
			public void create() throws DTOException, InitException {}
		};
	}

}
