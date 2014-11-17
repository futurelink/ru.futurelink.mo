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

package ru.futurelink.mo.orm.mailer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;

import ru.futurelink.mo.orm.ModelObject;
import ru.futurelink.mo.orm.annotations.DontCreateHistory;
import ru.futurelink.mo.orm.exceptions.SaveException;

@Entity(name = "MailQueue")
@Table(name = "MAIL_QUEUE")
@MappedSuperclass
public class MailQueue extends ModelObject {

	private static final long serialVersionUID = 1L;

	/**
	 * ID объекта
	 */
	@Id
	@GeneratedValue(generator="system-uuid")
	@DontCreateHistory
	@Column(name = "id", columnDefinition="VARCHAR(64)", nullable=false) 
	private		String id;	
	public 		String getId() {	return this.id;	}
	public		void setId(String id) { this.id = id; }

	@Column(name = "sent")
	private		boolean mSent;
	public		void setSent(boolean sent) { mSent = sent; }
	public		boolean getSent() { return mSent; }  
	
	@Column(name = "priority")
	private		int		mPriority;
	public		void	setPriority(int priority) { mPriority = priority; };
	public		int		getPriority() { return mPriority; }
	
	@Column(name = "reciever")
	private		String 	mReciever;	
	public 		String 	getReciever() {	return mReciever;	}
	public		void 	setReciever(String email) { mReciever = email; }

	@Column(name = "subject")
	private		String 	mSubject;	
	public 		String 	getSubject() {	return mSubject;	}
	public		void 	setSubject(String subj) { mSubject = subj; }

	@Lob
	@Column(name = "message")
	private		String 	mMessage;	
	public 		String 	getMessage() {	return mMessage;	}
	public		void 	setMessage(String msg) { mMessage = msg; }
	
	@Column(name = "attachments")
	private		String[]	mAttachments;
	public 		void 		setAttachments(String[] attachments) { mAttachments = attachments; }
	public		String[] 	getAttachments() { return mAttachments; }

	@Override
	public Boolean getDeleteFlag() {
		return false;
	}

	@Override
	public void setDeleteFlag(Boolean deleteFlag) {}
	
	@Override
	public Object save() throws SaveException {
		throw new SaveException("Save for mail queue messages is not allowed!", null);
	}
	@Override
	public void saveCommit() throws SaveException {
		throw new SaveException("Save for mail queue messages is not allowed!", null);		
	}  
}
