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
public interface IMailQueue extends IModelObject {
	public 		String getId();
	public		void setId(String id);

	public		void setSent(boolean sent);
	public		boolean getSent();  
	
	public		void	setPriority(int priority);
	public		int		getPriority();

	public 		String 	getReciever();
	public		void 	setReciever(String email);

	public 		String 	getSubject();
	public		void 	setSubject(String subj);

	public 		String 	getMessage();
	public		void 	setMessage(String msg);

	public 		void 		setAttachments(String[] attachments);
	public		String[] 	getAttachments();

}
