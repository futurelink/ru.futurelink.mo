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

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.toolbar.ItemToolbar;
import ru.futurelink.mo.web.composites.toolbar.JournalToolbar;
import ru.futurelink.mo.web.controller.CompositeParams;

/**
 * @author pavlov
 *
 */
public class CommonDataPickerToolbar extends ItemToolbar {
	private static final long serialVersionUID = 1L;

	private Button createButton;
	private Button viewButton;

	/**
	 * @param session
	 * @param parent
	 * @param style
	 */
	public CommonDataPickerToolbar(ApplicationSession session,
			Composite parent, int style, CompositeParams params) {
		super(session, parent, style);
		
		getSaveButton().setText(getLocaleString("select"));

		addSpacer();
		
		if ((params.get("allowCreate") != null) && ((boolean)params.get("allowCreate")) &&
			(params.get("itemControllerClass") != null)) {
			createButton = addButton("create");
			createButton.setImage(new Image(getDisplay(), JournalToolbar.class.getResourceAsStream("/images/32/add.png")));
			createButton.setText(getLocaleString("create"));
			createButton.addSelectionListener(new SelectionAdapter() {		
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					executeListener(createButton);
				}
			});
		}
		
		if (params.get("itemControllerClass") != null) {
			viewButton = addButton("edit");
			viewButton.setImage(new Image(getDisplay(), JournalToolbar.class.getResourceAsStream("/images/32/view.png")));
			viewButton.setText(getLocaleString("view"));
			viewButton.addSelectionListener(new SelectionAdapter() {		
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					executeListener(viewButton);
				}
			});
		}
	}

}
