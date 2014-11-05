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

import java.util.UUID;
import java.util.Vector;
 
import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.internal.databaseaccess.Accessor;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.sequencing.Sequence;
import org.eclipse.persistence.sessions.Session;
 
public class UUIDSequence extends Sequence implements SessionCustomizer {
 
	private static final long serialVersionUID = 1L;

	public UUIDSequence() {
		super();
	}
 
	public UUIDSequence(String name) {
		super(name);
	}
 
	@Override
	public Object getGeneratedValue(Accessor accessor,
			AbstractSession writeSession, String seqName) {
		return UUID.randomUUID().toString().toUpperCase();
	}
 
	@Override
	public Vector<?> getGeneratedVector(Accessor accessor,
			AbstractSession writeSession, String seqName, int size) {
		return null;
	}
 
	@Override
	public void onConnect() {
	}
 
	@Override
	public void onDisconnect() {
	}
 
	@Override
	public boolean shouldAcquireValueAfterInsert() {
		return false;
	}
 
	public boolean shouldOverrideExistingValue(String seqName,
			Object existingValue) {
		return ((String) existingValue).isEmpty();
	}
 
	@Override
	public boolean shouldUseTransaction() {
		return false;
	}
 
	@Override
	public boolean shouldUsePreallocation() {
		return false;
	}
 
	public void customize(Session session) throws Exception {
		UUIDSequence sequence = new UUIDSequence("system-uuid");
		session.getLogin().addSequence(sequence);
	}
 
}
