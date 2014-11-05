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

package ru.futurelink.mo.web.controller;

import org.eclipse.swt.widgets.TableColumn;

/**
 * @author pavlov
 *
 */
public interface CommonTableControllerListener {
	/**
	 * Обработка изменения размера колонки.
	 * 
	 * @param column
	 */
	public void onColumnResized(TableColumn column);
	
	/**
	 * Обработка добавления колонки в таблицу.
	 * 
	 * @param column
	 */
	public void onColumnAdded(TableColumn column, String filterField, String filterFieldGetter, String filterFieldSetter, Class<?> filterFieldType);
}
