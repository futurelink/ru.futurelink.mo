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

import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;

/**
 * Drag-and-drop decorator for composites.
 *
 * @author pavlov
 *
 */
public interface IDragDropDecorator {
	/**
	 * <p>Add drag feature for composite.</p>
     *
     * <p>It does nothing in composite itself but needs to be implemented in subclasses
     * to add that feature.</p>
	 *
	 * <p>For example if the composite contains TableViewer you must call addDragSupport for
     * it to enable dragging ability.</p>
	 * 
	 * @param operations
	 * @param transferTypes
	 * @param listener
	 */
	public void addDragSupport(int operations, Transfer[] transferTypes, DragSourceListener listener);
	
	/**
     * <p>Add drop feature for composite.</p>
     *
     * <p>It does nothing in composite itself but needs to be implemented in subclasses
     * to add that feature.</p>
     *
     * <p>For example if the composite contains TableViewer you must call addDropSupport for
     * it to enable dropping ability.</p>
	 * 
	 * @param operations
	 * @param transferTypes
	 * @param listener
	 */
	public void addDropSupport(int operations, Transfer[] transferTypes, DropTargetListener listener);
}
