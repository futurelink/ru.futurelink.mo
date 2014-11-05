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

package ru.futurelink.mo.orm.migration;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author pavlov
 *
 */
@Entity
@Table(name = "VERSION")
public class MigrationVersion {
	public MigrationVersion() {}

	@Id
	@GeneratedValue(generator="system-uuid")
	@Column(name = "id", columnDefinition="VARCHAR(64)", nullable=false)
	private String mId;
	public String getId() { return mId; }

	/**
	 * Номер версии базы данных.
	 */
	@Column(name = "version")
	private Integer mVersion;
	public Integer getVersion() { return mVersion; }	
	public void setVersion(Integer version) { mVersion = version; }
	
	/**
	 * Ревизия данной версии, из SVN или GIT.
	 */
	@Column(name = "revision")
	private String mRevision;
	public String getRevision() { return mRevision; }
	public void setRevision(String revision) { mRevision = revision; }
}
