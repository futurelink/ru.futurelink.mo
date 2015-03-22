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

package ru.futurelink.mo.web.composites;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;

import ru.futurelink.mo.web.app.ApplicationSession;

/**
 * Класс используется для интеграции CommonComposite в поп-ап окно, которое может быть
 * открыто из любого контроллера композита.
 * 
 * @author pavlov
 *
 */
public class CommonPopup extends Dialog {

	private static final long serialVersionUID = 1L;
	private Composite 	attachedComposite;
	private Object		result;
	private Control		lastFocusedControl;
	
	public CommonPopup(ApplicationSession session, Shell parent, int style) {
		super(parent, style);
	}

	public void setResult(Object result) {
		this.result = result;
	}
	
	public Object getResult() {
		return result;
	}

	final public Shell getShell() {
		return shell;
	}

	public void moveTo(Point p) {
		shell.setLocation (p.x, p.y);					
	}
	
	public void setSize(int width, int height) {
		shell.setSize(width, height);
	}
	
	public void hide() {		
		shell.setVisible(false);
	}
	
	public void show() {
		shell.setVisible(true);
	}
	
	/**
	 * Зацепить композит с содержимым
	 * для диалогового окна.
	 *  
	 * @param composite
	 */
	public void attachComposite(Composite composite) {
		attachedComposite = composite;

		attachedComposite.setParent(shell);
		attachedComposite.layout();

		//mAttachedComposite.setOwnerDialog(this);
		
		// Если уничтожается содержимое, закрываем диалог
		attachedComposite.addDisposeListener(new DisposeListener() {			
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetDisposed(DisposeEvent arg0) {
				shell.dispose();
			}
		});
	}

	public void close() {
		if (shell != null)
			shell.close();
	}
	
	public Integer open() {
		// В режиме диалога нужно проверить создался ли композит, если произошла
		// какая-то ошибка, по причине которой окно должно быть закрыто или не должно быть
		// открыто, то мы не открываем диалог вообще.		
		shell = new UnfocusableShell(getParent(), SWT.RESIZE | SWT.APPLICATION_MODAL);

		// Этот костыль нужен на случай если шелл будет принимать фокус
		lastFocusedControl = getShell().getDisplay().getFocusControl();
		
		FillLayout layout = new FillLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		shell.setLayout(layout);
		if ((attachedComposite != null) && (!attachedComposite.isDisposed())) {
			attachedComposite.setParent(shell);
			attachedComposite.layout();
		}
		shell.pack();
		shell.open();
			
		lastFocusedControl.setFocus();
	
		return 0;
	}
	
	class UnfocusableShell extends Shell {
		private static final long serialVersionUID = 1L;

		public UnfocusableShell(Shell parent, int style) {
			super(parent, style);
		}
		
		@Override
		public boolean setFocus() {
			return false;
		}
	}
}
