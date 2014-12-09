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

package ru.futurelink.mo.web.composites;

import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;

import ru.futurelink.mo.orm.exceptions.DTOException;

public class SimpleListDragListener implements DragSourceListener {

	private static final long serialVersionUID = 1L;
	private SimpleListComposite	mComposite;
	
	public SimpleListDragListener(SimpleListComposite composite) {
		mComposite = composite;
	}
	
	@Override
	public void dragStart(DragSourceEvent arg0) {		
		if (mComposite.getActiveData() == null)
			arg0.doit = false;	
	}
	
	@Override
	public void dragSetData(DragSourceEvent arg0) {
		if (TextTransfer.getInstance().isSupportedType(arg0.dataType)) {
			if (mComposite.getActiveData() != null) {
				try {				
					arg0.data = mComposite.getActiveData().getDataClass().getName() + 
						"@" + 
						mComposite.getActiveData().getId().toString();
				} catch (DTOException ex) {
					arg0.data = new String("");
					arg0.doit = false;
				}
			} else {
				arg0.data = new String("");
				arg0.doit = false;
			}
		}
	}
	
	@Override
	public void dragFinished(DragSourceEvent arg0) {
		if (mComposite.getActiveData() == null)
			arg0.doit = false;			
	}
}
