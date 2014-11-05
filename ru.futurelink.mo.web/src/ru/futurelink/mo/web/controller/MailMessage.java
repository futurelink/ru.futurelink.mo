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

package ru.futurelink.mo.web.controller;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.futurelink.mo.orm.mailer.MailTemplate;

public class MailMessage {
	private MailTemplate	mTemplate;
	private String			mTemplatedMessage;
	private String			mTemplatedSubject;
	private HashMap<String, String> mAttributes;
	
	private Pattern			mAtrributeRegex;

	public MailMessage(String templateId) {
		mAttributes = new HashMap<String, String>();
		mAtrributeRegex = Pattern.compile("%([A-Z0-9_]+)%");
	}

	public void setAttribute(String name, String value) {
		mAttributes.put(name, value);
	}

	public void setTemplate(String templateId) {
		
	}

	public void setTemplate(MailTemplate template) {
		mTemplate = template;
		
		if (mTemplate.getSubject() == null) mTemplate.setSubject("");
		if (mTemplate.getTemplate() == null) mTemplate.setTemplate("");
		
		fillAttributesFromTemplate();
	}

	private void applyAttributes() {
		// Заполняем темлпейт значениями и выводим
		mTemplatedMessage = mTemplate.getTemplate();
		mTemplatedSubject = mTemplate.getSubject();
		
		Iterator<Entry<String, String>> it = mAttributes.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, String> pairs = (Map.Entry<String, String>)it.next();
	        mTemplatedMessage = mTemplatedMessage.replaceAll("%"+pairs.getKey()+"%", pairs.getValue());
	        mTemplatedSubject = mTemplatedSubject.replaceAll("%"+pairs.getKey()+"%", pairs.getValue());
	    }
	    
	    System.out.println("Got message: "+mTemplatedMessage);
	}

	private void fillAttributesFromTemplate() {
		mAttributes.clear();
	
		// Смотрим что за вставки есть в темплейте и заполняем ими хеш аттрибутов
		Matcher matcher = mAtrributeRegex.matcher(
				new String(mTemplate.getSubject()) +
				" : " +
				new String(mTemplate.getTemplate()));
	    while (matcher.find()) {	      
	      setAttribute(matcher.group(1), "(no value)");
	    }
	}
	
	public String getSubject() {
		applyAttributes();
		return mTemplatedSubject;
	}
	
	@Override
	public String toString() {
		applyAttributes();
	    return mTemplatedMessage;
	    
	}
}
