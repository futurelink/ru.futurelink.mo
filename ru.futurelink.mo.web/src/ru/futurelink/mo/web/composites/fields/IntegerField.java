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

package ru.futurelink.mo.web.composites.fields;

import org.eclipse.rap.rwt.scripting.ClientListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;

import ru.futurelink.mo.orm.dto.FilterDTO;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.CommonItemComposite;
import ru.futurelink.mo.web.controller.CompositeParams;

/**
 * @author pavlov
 *
 */
public class IntegerField extends TextField {

	private static String digitsOnlyJS = 
			"var handleEvent = function( event ) {\n"
			+ "	var regexp = /^[0-9]*$/;\n"
			+ "	if( event.text.match( regexp ) === null ) {\n"
			+ "		event.doit = false;\n"
			+ "	}\n"
			+ "};\n"; 
 
	/**
	 * @param session
	 * @param parent
	 * @param style
	 * @param params
	 * @param c
	 */
	public IntegerField(ApplicationSession session, CommonComposite parent,
			int style, CompositeParams params, CommonItemComposite c) {
		super(session, parent, style, params, c);		
	}

	public IntegerField(ApplicationSession session, CommonComposite parent,
			int style, CompositeParams params, FilterDTO dto) {
		super(session, parent, style, params, dto);		
	}

	@Override
	protected void createControls(int style) {
		super.createControls(style);
		
		((Text)control).setText("");
	
		ClientListener clientListener = new ClientListener(digitsOnlyJS);
		control.addListener(SWT.Verify, clientListener);
	}
	
	@Override
	public Object getValue() {
		if (getText() == null || getText().equals("")) return (Integer)null;
		return Integer.valueOf(getText());
	}
	
	@Override
	public boolean isEmpty() {
		return (((Text)control).getText() == null) || ((Text)control).getText().isEmpty() || 
				(Integer.valueOf(((Text)control).getText()) == 0);
	}
}
