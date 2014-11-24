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

package ru.futurelink.mo.orm.iface;

/**
 * @author pavlov
 *
 */
public interface IHistoryObject extends ICommonObject {

	/**
	 * @return
	 */
	public String getNextId();	
	public void setPrevId(String prevId);

	/**
	 * @param b
	 */
	public void setOutdated(boolean b);
	public boolean getOutdated();

}
