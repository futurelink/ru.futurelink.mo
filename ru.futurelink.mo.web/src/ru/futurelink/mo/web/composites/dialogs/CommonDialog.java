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

package ru.futurelink.mo.web.composites.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;

/**
 * Класс используется для интеграции CommonComposite в диалоговое окно, которое
 * может быть открыто из любого контроллера композита.
 * 
 * @author pavlov
 *
 */
public class CommonDialog extends Dialog {

	private static final long serialVersionUID = 1L;

	public static int	SMALL = 1;	// Пятая часть размера экрана
	public static int	MEDIUM = 2; // Четверть размера экрана
	public static int	LARGE = 3; // Половина экрана
	public static int	FULL = 4; // На весь экран
	public static int FIXED = 5; // Фиксированный размер

	private CommonComposite 	mAttachedComposite;
	private int				mSize;
	private Object				mResult;
	private Rectangle			mFixedSize;
	
	public CommonDialog(ApplicationSession session, Shell parent, int style) {
		super(parent, style);

		style |= (SWT.CLOSE | SWT.TITLE | SWT.APPLICATION_MODAL);
		shell = new Shell(getParent(), style);
		
		mFixedSize = new Rectangle(0,0,0,0);
	}

	@Override
	public void setText(String string) {
		super.setText(string);
		
		shell.setText(getText());
	}
	
	/**
	 * Устаноить режим относительного размера диалога.
	 * 
	 * @param size
	 */
	public final void setSize(int size) {
		mSize = size;
	}
	
	/**
	 * Установить фиксированную ширину диалога для режима FIXED.
	 *  
	 * @param width
	 */
	public final void setFixedWidth(int width) {
		mFixedSize.width = width;
	}

	/**
	 * Установить фиксированную высоту диалога для режима FIXED.
	 * 
	 * @param height
	 */
	public final void setFixedHeight(int height) {
		mFixedSize.height = height;
	}
	
	private void applySize(int size) {
		Display primary = shell.getDisplay();
		Rectangle bounds = primary.getBounds ();
		if (mSize == SMALL) {			
			shell.setSize(bounds.width / 5, bounds.height / 3);
		} else if(mSize == MEDIUM) {
			shell.setSize(bounds.width / 4, bounds.height / 2);
		} else if(mSize == LARGE) {
			shell.setSize(bounds.width / 2, bounds.height / 2);
		} else if(mSize == FULL) {
			shell.setSize((int)(bounds.width * 0.9), (int)(bounds.height * 0.9));
		} else if(mSize == FIXED) {
			if (mFixedSize.width == 0) mFixedSize.width = bounds.width / 4;
			if (mFixedSize.height == 0) mFixedSize.height = bounds.height / 2;
			shell.setSize(mFixedSize.width, mFixedSize.height);
		}
	}
	
	public final void setResult(Object result) {
		mResult = result;
	}
	
	public final Object getResult() {
		return mResult;
	}
	
	/**
	 * Зацепить композит с содержимым
	 * для диалогового окна.
	 *  
	 * @param composite
	 */
	public void attachComposite(CommonComposite composite) {
		mAttachedComposite = composite;
		mAttachedComposite.setOwnerDialog(this);
		
		// Если уничтожается содержимое, закрываем диалог
		mAttachedComposite.addDisposeListener(new DisposeListener() {			
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetDisposed(DisposeEvent arg0) {
				getShell().dispose();
			}
		});
	}
	
	/**
	 * Открыть диалоговое окно в модальном режиме.
	 * 
	 * @return -1 если произошла ошибка открытия, не существует или уничтожено содержимое
	 * 0 - если все завершилось нормально. 
	 */
	public Integer open() {
		// В режиме диалога нужно проверить создался ли композит, если произошла
		// какая-то ошибка, по причине которой окно должно быть закрыто или не должно быть
		// открыто, то мы не открываем диалог вообще.
		if ((mAttachedComposite != null) && (!mAttachedComposite.isDisposed())) {
			FillLayout layout = new FillLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;

			shell.setLayout(layout);
			mAttachedComposite.setParent(shell);
			mAttachedComposite.layout();
			
			shell.pack();
			shell.open();

			applySize(mSize);
			
			Display primary = shell.getDisplay();
			Rectangle bounds = primary.getBounds ();
			Rectangle rect = shell.getBounds ();
			int x = bounds.x + (bounds.width - rect.width) / 2;
			int y = bounds.y + (bounds.height - rect.height) / 2;
			shell.setLocation (x, y);			
			
			Display display = getParent().getDisplay();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
			return 0;
		} else {
			throw new IllegalStateException("No composite attached to dialog window");
		}		
	}
	
	final public Shell getShell() {
		return shell;
	}
}
