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
import javax.persistence.MappedSuperclass;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import ru.futurelink.mo.orm.entities.CommonObject;
import ru.futurelink.mo.orm.pm.PersistentManagerSession;

/**
 * @author pavlov
 *
 */
@Entity (name = "MailServer")
@Table(name = "MAIL_SERVERS")
@MappedSuperclass
@NamedQueries({
	@NamedQuery(name="MailServers.all", query="SELECT u FROM MailServer u where u.deleteFlag = 0")
})
public class MailServer extends CommonObject {

	private static final long serialVersionUID = 1L;

	/**
	 * Конструктор сделан приватным, 
	 * чтобы его нельзя было использовать.
	 */
	protected MailServer() {}

	public MailServer(PersistentManagerSession manager) {
		super(manager);
	}

	/**
	 * Название сервера
	 */
	@Column(name = "name")
	private		String	mName;
	public		String	getName() { return mName; }
	public		void	setName(String name) { mName = name; }

	/**
	 * Адрес сервера
	 */
	@Column(name = "address")
	private		String	mAddress;
	public		String	getAddress() { return mAddress; }
	public		void	setAddress(String addr) { mAddress = addr; }

	/**
	 * SMTP логин
	 */
	@Column(name = "smtplogin")
	private		String	mSMTPLogin;
	public		String	getSMTPLogin() { return mSMTPLogin; }
	public		void	setSMTPLogin(String login) { mSMTPLogin = login; }

	/**
	 * SMTP пароль
	 */
	@Column(name = "smtppassword")
	private		String	mSMTPPassword;
	public		String	getSMTPPassword() { return mSMTPPassword; }
	public		void	setSMTPPassword(String pass) { mSMTPPassword = pass; }

	/**
	 * SMTP порт
	 */
	@Column(name = "smtpport")
	private		String	mSMTPPort;
	public		String	getSMTPPort() { return mSMTPPort; }
	public		void	setSMTPPort(String port) { mSMTPPort = port; }

	/**
	 * Тип авторизации
	 */
	@Column(name = "smtpauth")
	private		String	mSMTPAuth;
	public		String	getSMTPAuth() { return mSMTPAuth; }
	public		void	setSMTPAuth(String auth) { mSMTPAuth = auth; }

	/**
	 * Отправитель
	 */
	@Column(name = "smtpsender")
	private		String	mSMTPSender;
	public		String	getSMTPSender() { return mSMTPSender; }
	public		void	setSMTPSender(String sender) { mSMTPSender = sender; }
	
}
