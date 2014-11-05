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

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Control;

/**
 * @author pavlov
 *
 */
@Deprecated
public class FocusTraversalListener implements FocusListener {
	private static final long serialVersionUID = 1L;

	private Control mTraverseToComposite;
	
	/**
	 * 
	 */
	public FocusTraversalListener(Control traverseToComposite) {
		mTraverseToComposite = traverseToComposite;
	}
	
	@Override
	public void focusGained(FocusEvent arg0) {
		if (mTraverseToComposite != null)
			mTraverseToComposite.setFocus();
	}

	@Override
	public void focusLost(FocusEvent arg0) {
		
	}

}
