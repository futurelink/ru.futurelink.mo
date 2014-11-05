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

package ru.futurelink.mo.web.topmenu;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;

import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.controller.CompositeParams;

public class TopMenuDropdown extends CommonComposite {
	private static final long serialVersionUID = 1L;

	private Label 	mDropdownLabel;
	private Label 	mArrow;
	private Color	mActiveColor;
	private Control mSelf;
	private Menu	mPullDownMenu;
	
	public TopMenuDropdown(ApplicationSession session, Composite parent,
			int style, CompositeParams params) {
		super(session, parent, style, params);

		mSelf = this;
	
		setData(RWT.CUSTOM_VARIANT, "topMenuDropDown");
		
		mPullDownMenu = new Menu( parent.getShell(), SWT.POP_UP );
		mPullDownMenu.setData( RWT.CUSTOM_VARIANT, "navigation" );
		
		MouseListener mListener = new MouseListener() {			
			private static final long serialVersionUID = 1L;

			@Override
			public void mouseUp(MouseEvent event) {
				setBackground(null);
			}
			
			@Override
			public void mouseDown(MouseEvent event) {
				if (mActiveColor != null)
					setBackground(mActiveColor);
				
				Point location = mSelf.toDisplay(mSelf.getLocation());
				int y = mSelf.getSize().y + location.y;
				int x = location.x;
		        openMenu( new Point(x, y) );				
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				
			}
		};
		
		GridLayout l = new GridLayout();
		l.numColumns = 2;
		l.marginWidth = 8;
		l.marginHeight = 8;
		setLayout(l);

		mDropdownLabel = new Label(this, SWT.NONE);
		mDropdownLabel.setData(RWT.CUSTOM_VARIANT, "topMenuElement");
		mDropdownLabel.setForeground(new Color(getDisplay(), 255, 255, 255));
		mDropdownLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_CENTER | GridData.GRAB_VERTICAL));		
	
		mArrow = new Label(this, SWT.NONE);
		mArrow.setData(RWT.CUSTOM_VARIANT, "topMenuElement");
		mArrow.setForeground(new Color(getDisplay(), 255, 255, 255));
		mArrow.setText("\u25BC");
		mArrow.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_CENTER | GridData.GRAB_VERTICAL));
		
		mDropdownLabel.addMouseListener(mListener);
		mArrow.addMouseListener(mListener);
		addMouseListener(mListener);
	}

	public void setText(String text) {
		mDropdownLabel.setText(text);
	}

	public void setImage(Image img) {
		mDropdownLabel.setImage(img);
	}
	
	public void setLabelData(String key, String value) {
		mDropdownLabel.setData(key, value);
	}
	
	public void setActiveColor(Color color) {
		mActiveColor = color;
	}
	
	private void openMenu( Point point ) {
	    mPullDownMenu.setLocation( point );
	    mPullDownMenu.setVisible( true );
	}
	
	public Menu getMenu() {
		return mPullDownMenu;
	}
}
