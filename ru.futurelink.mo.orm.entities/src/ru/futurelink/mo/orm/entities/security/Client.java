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

package ru.futurelink.mo.orm.entities.security;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import ru.futurelink.mo.orm.entities.CommonObject;
import ru.futurelink.mo.orm.pm.PersistentManagerSession;

@Entity(name = "Client")
@Table(name = "CLIENTS")
@NamedQueries({
	@NamedQuery(name="Client.keyExists", query="SELECT c FROM Client c where c.owner = :owner and c.secret = :secret and c.deleteFlag = 0")
})
public class Client extends CommonObject {
	private static final long serialVersionUID = 1L;

	protected Client() {}

	public Client(PersistentManagerSession manager) {
		super(manager);
	}
	
	@Column(name = "secret")
	private String secret;
	public void setSecret(String secret) { this.secret = secret; }
	public String getSecret() { return secret; }  
}
