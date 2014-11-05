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

package ru.futurelink.mo.web.controller.iface;

import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;

/**
 * @author pavlov
 *
 */
public interface ICompositeController 
	extends IController, ISessionDecorator, IBundleDecorator 
{
	public Composite getComposite();
	public Composite getContainer();
	
	public void processUsecaseParams();
	
	public void reparentComposite(Composite newParent);

	public void clear();
	public void refreshBySender(String sender, boolean refreshSubcontrollers) throws Exception;
	public void addDropSupport(int operations, Transfer[] transferTypes, DropTargetListener listener);
	public ICompositeController getParentController();
}
