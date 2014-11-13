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

package ru.futurelink.mo.orm.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import ru.futurelink.mo.orm.dto.access.IDTOAccessChecker;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.pm.IPersistentManagerSession;

/**
 * DTO object list implementation.
 *
 * The list ordering is provided by intertal ordering map which
 * can be aquired by getOrderList. The key of ordering map is the ID
 * of data contained by DTO.
 *
 * Access checking agent is provided on list construction to all list DTOs.
 *
 * @author pavlov
 *
 */
public abstract class CommonDTOList<T extends IDTO> {
	private TreeMap<Integer, String>	mOrderList;	// Ordering map
	private HashMap<String, T>			mDTOList;	// Data map

	private IDTOAccessChecker			mAccessChecker;
	private IPersistentManagerSession	mPersistent;

	private Class<T>					mDTOClass;
	private Integer						mIndex;		// Ordering index

	public CommonDTOList(IPersistentManagerSession persistent, IDTOAccessChecker accessChecker, Class<T> DTOclass) {
		mPersistent = persistent;
		mAccessChecker = accessChecker;
		mDTOClass = DTOclass;

		mOrderList = new TreeMap<Integer, String>();
		mDTOList = new HashMap<String, T>();
		mIndex = 0;
	}
	
	/**
	 * Get DTO map from list. The key of this map is item ID.
	 * 
	 * @return
	 */
	public Map<String, T> getDTOList() {
		return mDTOList;
	}

	/**
	 * Get list ordering structure.
	 * 
	 * @return
	 */
	public Map<Integer, String> getOrderList() {
		return mOrderList;
	}
	
	/**
	 * Get DTO item by order list index (see getOrderList).
	 * 
	 * @param index
	 * @return
	 */
	public T getDTOItem(Integer index) {
		return mDTOList.get(mOrderList.get(index));
	}

	/**
	 * Add DTO object to list.
	 * 
	 * @param dtoItem
	 * @throws DTOException 
	 */
	@SuppressWarnings("unchecked")
	public void addDTOItem(IDTO dtoItem) throws DTOException {
		// Добавляем только новые элементы, если элемент уже есть,
		// его дублировать не надо.
		if (!mDTOList.containsValue(dtoItem)) {
			String id = dtoItem.getId();
			// Если ID есть, то добавляем элемент с его ID, иначе,
			// добавляем элемент с временной ID, которая создается на базе
			// внутренненго индекса.
			if ((id == null) || id.isEmpty()) { id = "ID_"+String.valueOf(mIndex); }
			mOrderList.put(mIndex, id);
			mDTOList.put(id, (T) dtoItem);
			mIndex++;
		}
	}

	/**
	 * Remove DTO item from list.
	 * 
	 * @param dtoItem
	 * @throws DTOException
	 */
	public void removeDTOItem(T dtoItem) throws DTOException {
		for (Integer index : mOrderList.keySet()) {
			if (mOrderList.get(index).equals(dtoItem.getId())) {
				mOrderList.remove(index);
				mDTOList.remove(dtoItem.getId());
				return;
			}
		}
	}
	
	/**
	 * Clear the list.
	 */
	public void clear() {
		mOrderList.clear();
		mDTOList.clear();
		mIndex = 0;
	}
	
	/**
     * Get access checker agent.
     *
	 * @return
	 */
	protected IDTOAccessChecker getAccessChecker() {
		return mAccessChecker;
	}

	/**
     * Get DTO item class.
     *
	 * @return
	 */
	protected Class<T> getDTOClass() {
		return mDTOClass;
	}

	/**
	 * @return
	 */
	protected IPersistentManagerSession getPersistentManagerSession() {
		return mPersistent;
	}
}
