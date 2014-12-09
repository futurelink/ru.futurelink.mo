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

package ru.futurelink.mo.web.topmenu;

import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.orm.iface.ICommonObject;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.controller.CompositeController;
import ru.futurelink.mo.web.controller.CompositeParams;

abstract public class TopMenuController extends CompositeController {

	public TopMenuController(ApplicationSession session,
			Class<? extends ICommonObject> dataClass) {
		super(session, dataClass);
	}

	public TopMenuController(CompositeController parentController, 
			Class<? extends ICommonObject> dataClass, 
			Composite container, CompositeParams compositeParams) {
		super(parentController, dataClass, container, compositeParams);
	}

	@Override
	protected CommonComposite createComposite(CompositeParams params) {
		return null;
	}

	@Override
	protected void doBeforeCreateComposite() {
	}

	@Override
	protected void doAfterCreateComposite() {
	}

	@Override
	protected void doBeforeInit() {
	}

	@Override
	protected void doAfterInit() {	
	}

	@Override
	public void processUsecaseParams() {
	}
}
