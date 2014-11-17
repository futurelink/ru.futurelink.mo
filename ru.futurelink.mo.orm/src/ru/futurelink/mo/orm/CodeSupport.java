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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.JoinColumn;

/**
 *
 * @author Futurelink
 * @since 0.1
 *
 */
@Entity(name = "CodeSupport")
@Table(name = "CODES")
public class CodeSupport {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	public Long getId() { return id; }

	@JoinColumn(name = "object", referencedColumnName="id")
	private CommonObject mObject;
	public  CommonObject getObject() { return mObject; }
	public void setObject(CommonObject object) { mObject = object; }

	@Column(name = "objectClass")
	private String mObjectClass;
	public void setObjectClass(String className) { mObjectClass = className; }
	public String getObjectClass() { return mObjectClass; }

}
