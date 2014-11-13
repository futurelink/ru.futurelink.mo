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

import java.util.ArrayList;

import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.orm.dto.access.DTOAccessException;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.exceptions.SaveException;
import ru.futurelink.mo.orm.security.User;

/**
 * Объект DTO с встроенной подпиской. Работает с двумя типами объектов - 
 * шареным объектом и объектом подписки на этот шареный объект.
 * 
 * @author pavlov
 *
 */
public class EditorDTOWithLinkage extends EditorDTO
	implements IDTOLinkage {
	private static final long serialVersionUID = 1L;

	private EditorDTO		mLinkageDTO;

	public EditorDTOWithLinkage(CommonObject dataItem) {
		super(dataItem);
	}

	@Override
	public void setLinkedItem(CommonObject linkageItem) {
		mLinkageDTO = new EditorDTO(linkageItem);
		mLinkageDTO.addAccessChecker(mAccessChecker);
	}

	@Override
	public EditorDTO getLinkedDTO() {
		return mLinkageDTO;
	}

	@Override
	public void save() throws DTOException, SaveException {
		if (mAccessChecker == null) {
			throw new DTOAccessException("Не установлен агент проверки прав доступа, операция невозможна", null);
		}

		// Если объект DTO, к которому создана подписка - мой, то надо его сохранить.
		// В противном случае - пропускаем его.
		if (getCreator().equals(mLinkageDTO.getCreator())) {
			super.save();
		}

		// Надо созхранить данные только по подписке, т.к. основной объект
		// не является объектом, принадлежащим тому пользователю, который имеет подписку.
		if (getLinkedDTO() != null) {
			getLinkedDTO().save();
		} else {
			throw new SaveException("Ошибка сохранения подписки, подписка = null!", null);
		}
	}

	@Override
	public void activateLinkage() throws DTOException {
		mLinkageDTO.recover();
	}

	@Override
	public void deactivateLinkage() throws DTOException {
		mLinkageDTO.delete(false);		
	}

	@Override
	public Boolean getLinkageActive() throws DTOException {
		return !mLinkageDTO.getDeleteFlag();
	}

	@Override
	public Boolean getLinkagePersisted() throws DTOException {
		return mLinkageDTO.getId() != null && !mLinkageDTO.getId().equals("");
	}

	@Override
	public User getLinkageCreator() {
		return mLinkageDTO.getCreator();
	}

	/**
	 * Метод переопределен, помимо изменений в основном DTO он
	 * также возвращает изменения в DTO подписки.
	 */
	@Override
	public ArrayList<String> getChangedData() throws DTOException {
		ArrayList <String> t = new ArrayList<String>();
		t.addAll(super.getChangedData());
		t.addAll(mLinkageDTO.getChangedData());
		
		return t;
	}
}
