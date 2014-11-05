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

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Cool gradiented separator styled from CSS "gradientedSeparator"
 * class. It's a simple Composite subclass.
 *
 * @author pavlov
 *
 */
public class GradientedSeparator extends Composite {
	private static final long serialVersionUID = 1L;

	/**
	 * @param parent
	 * @param style
	 */
	public GradientedSeparator(Composite parent, int style) {
		super(parent, style | SWT.NO_FOCUS);

		setData(RWT.CUSTOM_VARIANT, "gradientedSeparator");
		setBackgroundMode(SWT.INHERIT_NONE);
	}

}
