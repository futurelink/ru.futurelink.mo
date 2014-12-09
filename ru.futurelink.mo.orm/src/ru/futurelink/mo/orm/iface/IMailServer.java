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
public interface IMailServer extends ICommonObject {
	public		String	getName();
	public		void	setName(String name);

	public		String	getAddress();
	public		void	setAddress(String addr);

	public		String	getSMTPLogin();
	public		void	setSMTPLogin(String login);

	public		String	getSMTPPassword();
	public		void	setSMTPPassword(String pass);

	public		String	getSMTPPort();
	public		void	setSMTPPort(String port);

	public		String	getSMTPAuth();
	public		void	setSMTPAuth(String auth);

	public		String	getSMTPSender();
	public		void	setSMTPSender(String sender);
}
