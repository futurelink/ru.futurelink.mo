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

package ru.futurelink.mo.orm.entities;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.JoinColumn;

import ru.futurelink.mo.orm.iface.ICodeSupport;
import ru.futurelink.mo.orm.iface.ICommonObject;

/**
 *
 * @author Futurelink
 * @since 0.1
 *
 */
@Entity(name = "CodeSupport")
@Table(name = "CODES")
public class CodeSupport implements ICodeSupport {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	public Long getId() { return id; }

	@OneToMany(targetEntity=CommonObject.class)
	@JoinColumn(name = "object", referencedColumnName="id")
	private Set<ICommonObject> mObjects;
	public  Set<ICommonObject> getObject() { return mObjects; }
	//public void setObject(ICommonObject object) { mObject = (CommonObject) object; }

	@Column(name = "objectClass")
	private String mObjectClass;
	public void setObjectClass(String className) { mObjectClass = className; }
	public String getObjectClass() { return mObjectClass; }

}
