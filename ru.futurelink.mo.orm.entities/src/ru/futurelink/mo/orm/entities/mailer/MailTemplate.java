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

package ru.futurelink.mo.orm.entities.mailer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import ru.futurelink.mo.orm.entities.CommonObject;
import ru.futurelink.mo.orm.iface.IMailTemplate;
import ru.futurelink.mo.orm.pm.PersistentManagerSession;

@Entity (name = "MailTemplate")
@Table(name = "MAIL_TEMPLATE")
@MappedSuperclass
@NamedQueries({
	@NamedQuery(name="MailTemplate.findByName", query="SELECT u FROM MailTemplate u where u.mName = :name and u.deleteFlag = 0"),
	@NamedQuery(name="MailTemplate.all", query="SELECT u FROM MailTemplate u where u.deleteFlag = 0")
})
public class MailTemplate extends CommonObject 
	implements IMailTemplate {

	private static final long serialVersionUID = 1L;

	/**
	 * Конструктор сделан приватным, 
	 * чтобы его нельзя было использовать.
	 */
	protected MailTemplate() {}

	public MailTemplate(PersistentManagerSession manager) {
		super(manager);

		mSubject = "";
		mTemplate = "";
	}

	@Column(name = "name")
	private		String	mName;
	public		String	getName() { return mName; }
	public		void	setName(String name) { mName = name; }

	@Column(name = "subject")
	private		String 	mSubject;
	public 		String 	getSubject() { return mSubject; }
	public		void 	setSubject(String subj) { mSubject = subj; }

	@Lob
	@Column(name = "template")
	private		String mTemplate;
	public		void setTemplate(String template) { mTemplate = template; }
	public		String getTemplate() { return mTemplate; }
	
	@Column(name = "locale")
	private		String	mLocale;
	public		String	getLocale() { return mLocale; }
	public		void	setLocale(String locale) { mLocale = locale; }
}
