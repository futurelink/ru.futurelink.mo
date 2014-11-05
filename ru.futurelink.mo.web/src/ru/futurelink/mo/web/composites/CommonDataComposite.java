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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.toolbar.CommonToolbar;
import ru.futurelink.mo.web.controller.CompositeParams;
import ru.futurelink.mo.web.exceptions.CreationException;
import ru.futurelink.mo.web.exceptions.InitException;

/**
 * <p>This class is a first layer of composites that have an access to data model through CommonDTO
 * objects.</p>
 *
 * <p>The composite contains pre-created layout with toolbar and workspace compsites. To complete
 * UI creation two methods must be implemented: createToolbar() and createWorkspace().</p>
 *
 * <p>Toolbar can be placed on top of workspace (default) or below the workspace. This position
 * can be switched passing parameter "toolbarPosition" to composite constructor via CompositeParams.</p>
 *
 * <p><i>This class is to be overridden and abstract methods are to be implemented.</i></p>
 * 
 * @author pavlov
 *
 */
public abstract class CommonDataComposite extends CommonComposite
{
	private static final long serialVersionUID = 1L;

    enum        ToolBarPosition    { TOP, BOTTOM };

	private		Label 				mCaptionLabel;
	protected 	CommonToolbar 		mToolbar;
	protected 	CommonComposite 	mWorkspace;
	
	public CommonDataComposite(ApplicationSession session, Composite parent, int style, CompositeParams params) {
		super(session, parent, style, params);	
	}

	protected CommonDataComposite(Composite parent) { super(parent); }

	@Override
	public void init() throws InitException {
		super.init();

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 8;
		setLayout(layout);

		// Create composite contents
		try {			
			createContents();
		} catch(CreationException ex) {
			layout = null;
			setLayout(null);
			throw new InitException(ex.getMessage());
		}

		if (mToolbar != null) {
			mToolbar.setLayoutData(
					new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL)
				);
			mToolbar.pack();
		}
		if (mWorkspace != null) {
			mWorkspace.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | 
					GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | 
					GridData.GRAB_VERTICAL));
			mWorkspace.pack();
		}	
		
		layout();
	}
	
	/**
	 * Sets composite caption label text.
	 * 
	 * @param caption
	 */
	final public void setCaption(String caption) {
		mCaptionLabel.setText(caption);
		if (!caption.equals("")) {
			setCaptionLabelVisible(true);
		} else {
			setCaptionLabelVisible(false);
		}
	}

    /**
     * Show or hide composite caption label.
     *
     * @param visible
     */
	final private void setCaptionLabelVisible(boolean visible) {
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		if (!visible) { gd.exclude = true; }
		
		mCaptionLabel.getParent().setVisible(visible);
		mCaptionLabel.getParent().pack();
		mCaptionLabel.getParent().setLayoutData(gd);
		layout();
	}
	
	/**
	 * Creates composite caption label. Reimplement to create custom label or change its style.
	 */
	final protected void createCaptionLabel() {
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;

		Composite c = new Composite(this, SWT.NONE);
		c.setForeground(new Color(c.getDisplay(), 255, 255, 255));
		c.setBackground(new Color(c.getDisplay(), 50, 100, 150));
		c.setLayoutData(gd);

		c.setBackgroundMode(SWT.INHERIT_DEFAULT);
		
		GridLayout cg = new GridLayout();
		cg.marginWidth = 16;
		cg.marginHeight = 8;
		cg.numColumns = 1;
		c.setLayout(cg);
		
		mCaptionLabel = new Label(c, SWT.NONE);
		FontData[] fD = mCaptionLabel.getFont().getFontData(); fD[0].setHeight(20);
		mCaptionLabel.setFont( new Font(mCaptionLabel.getDisplay(), fD[0]));
		mCaptionLabel.setForeground(new Color(c.getDisplay(), 255, 255, 255));
		mCaptionLabel.setLayoutData(gd);

		setCaptionLabelVisible(false);
	}

	protected void createContents() throws CreationException {
		//createCaptionLabel();

        // Default toolbar location is on top of workspace
        if (getParam("toolbarPosition") != ToolBarPosition.BOTTOM)
            mToolbar = createToolbar();

        mWorkspace = createWorkspace();

        if (getParam("toolbarPosition") == ToolBarPosition.BOTTOM)
            mToolbar = createToolbar();

		// Если не получилось создать рабочее пространство
		// отменяем создание всего.
		/*if (mWorkspace == null) {
			if ((mToolbar != null) && (!mToolbar.isDisposed()))
				mToolbar.dispose();
				mToolbar = null;
				dispose();
		}*/
	}
	
	/**
	 * Get toolbar instance.
	 * 
	 * @return
	 */
	public CommonToolbar getToolbar() {
		return mToolbar;
	}

    /**
     * Get workspace instance.
     *
     * @return
     */
	public CommonComposite getWorkspace() {
		return mWorkspace;
	}
	
	/**
	 * Disable whole toolbar by disabling all of its controls.
	 */
	public void disableToolbar() {
		if (mToolbar != null)
			mToolbar.setControlsEnabled(false);		
	}

    /**
     * Enable or disable single toolbar tool by its name.
     *
     * @param toolName
     * @param enabled
     */
	public void setToolEnabled(String toolName, boolean enabled) {
		if (mToolbar != null)
			mToolbar.setControlEnabled(toolName, enabled);
	}
	
	/**
	 * Create composite workspace.
     *
     * <i>Implement it in subclasses.</i>
     *
	 * @throws CreationException 
	 */
	protected abstract CommonComposite createWorkspace() throws CreationException;
	
	/**
	 * Creates composite toolbar.
     *
     * <i>Implement it in subclasses.</i>
	 */
	protected abstract CommonToolbar createToolbar();
}
