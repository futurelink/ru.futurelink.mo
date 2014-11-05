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

package ru.futurelink.mo.orm;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * Лог работы пользователей с элементами данных.
 * Каждый элемент лога должен быть связан туда и обратно
 * с элементом данных. То есть из элемента данных можно получить
 * запись лога, а из записи лога можно получить элемент данных,
 * который был изменен и при этом изменении запись была создана.
 * 
 * @author Futurelink
 *
 */
@Entity(name = "WorkLogSupport")
@Table(name = "LOG_WORK")
public class WorkLogSupport extends CommonObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected WorkLogSupport() {}

	public WorkLogSupport(PersistentManagerSession manager) {
		super(manager);
	}
	
	@Column(name="objectClassName")
	private String mObjectClassName;
	public void setObjectClassName(String ocn) { mObjectClassName = ocn; }
	public String getObjectClassName() { return mObjectClassName; }
	
	@Column(name="objectId")
	private String mObjectId;
	public void setObjectId(String oid) { mObjectId = oid; }
	public String getObjectId() { return mObjectId; }   
	
	/**
	 * Краткое описание того, что случилось в элементе.
	 */
	@Column(name="description")
	private String mDescription;
	public String 	getDescription() { return mDescription; }
	public void	setDescription(String description) { mDescription = description; }
}
