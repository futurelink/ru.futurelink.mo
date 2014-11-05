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

package ru.futurelink.mo.web.composites.fields.linked;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.controller.CompositeParams;

/**
 * @author pavlov
 *
 */
public class LinkedListComposite extends CommonComposite {
	private static final long serialVersionUID = 1L;

	private List 				mList;
	private ArrayList<String>	mItems;	// Ссылка на список будет существовать пока есть композит
	
	@Override
	public boolean setFocus() {
		return false;
	}
	
	/**
	 * @param session
	 * @param parent
	 * @param style
	 * @param params
	 */
	public LinkedListComposite(ApplicationSession session, Composite parent,
			int style, CompositeParams params) {
		super(session, parent, style, params);
		
		setLayout(new FillLayout());
		
		mList = new List(this, SWT.NONE);
		mList.addMouseListener(new MouseListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void mouseUp(MouseEvent arg0) {}
			
			@Override
			public void mouseDown(MouseEvent arg0) {}
			
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				// Двойной клик - выбор элемента и ввод.
				if (getControllerListener() != null)
					((LinkedListControllerListener)getControllerListener()).autoCompleteEntered();				
			}
		});
		
		mList.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// Выбрали какой-то элемент из списка, передаем контроллеру.
				if (getControllerListener() != null)
					((LinkedListControllerListener)getControllerListener()).autoCompleteSelected(
						mItems.get(mList.getSelectionIndex()));
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {}
		});
	}

	public void setInput(ArrayList<String> items) {
		mItems = items;
		refresh();
	}

	public void refresh() {
		if (mItems != null) {
			mList.setItems(mItems.toArray(new String[mList.getItemCount()]));
		} else {
			mList.removeAll();
		}
	}

	/**
	 * Выбрать следующий элемент списка.
	 */
	public final void selectNext() {
		int index = mList.getSelectionIndex();
		if (index < mList.getItemCount()) {
			mList.select(index+1);
		}
	}

	/**
	 * Выбрать предыдущий элемент списка.
	 */
	public final void selectPrev() {
		int index = mList.getSelectionIndex();
		if (index > 0) {
			mList.select(index-1);
		} else {
			mList.setSelection(-1);
		}
	}

	/**
	 * Получить выбранный элемент.
	 * 
	 * @return
	 */
	public String getSelected() {
		if ((mList != null) && (!mList.isDisposed())) {
			if (mList.getSelectionIndex() < 0) return null;
			return mList.getItem(mList.getSelectionIndex());
		}
		return null;
	}
}
