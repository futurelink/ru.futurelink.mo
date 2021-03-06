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

package ru.futurelink.mo.orm.entities.helpers;

import org.eclipse.persistence.logging.AbstractSessionLog;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.logging.SessionLogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SLF4JSessionLogger  extends AbstractSessionLog implements SessionLog {
	public static final Logger LOG = LoggerFactory.getLogger(SLF4JSessionLogger.class);

	public void log(SessionLogEntry sessionLogEntry) {
		switch (sessionLogEntry.getLevel()) {
			case SEVERE:
				LOG.error(sessionLogEntry.getMessage(), sessionLogEntry.getException());
				break;
			case WARNING:
				LOG.warn(sessionLogEntry.getMessage());
				break;
			case INFO:
				LOG.info(sessionLogEntry.getMessage());
				break;
			default:
				LOG.debug(sessionLogEntry.getMessage());
       	}
	}
}
