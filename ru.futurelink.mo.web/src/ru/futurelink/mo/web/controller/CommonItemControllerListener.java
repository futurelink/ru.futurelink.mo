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

import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.composites.fields.IField;

/**
 * Обработчик событий происходящих на элементах подключенных
 * к контроллеру. Обработчик необходимо переопределить для любого
 * нестандартного контроллера, добавив методы обработки событий.
 * 
 * Данный обработчик реализует основные события формы. Реализовать его
 * следует на контроллере, а в методе doAfterCreateComposite контроллера
 * привязать к форме. В классе формы (композита) следует вызывать 
 * getControllerListener() для получения объекта обработчика и вызывать
 * его методы.
 * 
 * @author pavlov_d
 *
 */
public interface CommonItemControllerListener extends CommonControllerListener {
	public void dataChanged(IField editor) throws DTOException;
	public void dataChangeFinished(IField editor) throws DTOException;
}
