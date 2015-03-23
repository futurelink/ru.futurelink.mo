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

package ru.futurelink.mo.web.composites.fields.datapicker.as;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ru.futurelink.mo.orm.dto.CommonDTOList;
import ru.futurelink.mo.orm.dto.IDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.CommonListComposite;
import ru.futurelink.mo.web.composites.table.CommonContentProvider;
import ru.futurelink.mo.web.composites.toolbar.CommonToolbar;
import ru.futurelink.mo.web.composites.toolbar.JournalToolbar;
import ru.futurelink.mo.web.controller.CommonListControllerListener;
import ru.futurelink.mo.web.controller.CompositeParams;
import ru.futurelink.mo.web.exceptions.CreationException;

/**
 * @author pavlov
 *
 */
public class AutosuggestComposite extends CommonListComposite {

	private static final long serialVersionUID = 1L;

	private String displayColumnName;
	private Text autoSuggestEdit;
	private ListLabelProvider labelProvider;
	private ListViewer	autoSuggestListView;

	/**
	 * @param session
	 * @param parent
	 * @param style
	 * @param params
	 */
	public AutosuggestComposite(ApplicationSession session,
			Composite parent, int style, CompositeParams params) {
		super(session, parent, style, params);
		
		labelProvider = new ListLabelProvider("");
	}

	public void setDisplayColumn(String columnName) {
		displayColumnName = columnName;
		labelProvider = new ListLabelProvider(displayColumnName);
		autoSuggestListView.setLabelProvider(labelProvider);
	}
	
	@Override
	public void setInput(CommonDTOList<? extends IDTO> input)
			throws DTOException {
		autoSuggestListView.setInput(input);		
	}

	@Override
	public void refresh() throws DTOException {
		if (getDTO() != null)
			setInput(getDTO());
	}

	@Override
	public void selectById(String id) throws DTOException {
		
	}

	@Override
	public void selectByDTO(IDTO dto) {
		
	}

	@Override
	protected CommonComposite createWorkspace() throws CreationException {
		GridLayout gl = new GridLayout();
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		gl.marginTop = 0;
		gl.marginLeft = 0;
		gl.marginRight = 0;
		gl.marginBottom = 0;
		
		CommonComposite c = new CommonComposite(getSession(), this, SWT.NONE, new CompositeParams());
		c.setLayout(gl);

		/*
		 * Top composite
		 */
		Composite top = new Composite(c, SWT.NONE);
		top.setLayout(new GridLayout(2, false));
		top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		autoSuggestEdit = new Text(top, SWT.BORDER);
		autoSuggestEdit.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		autoSuggestEdit.addModifyListener(new ModifyListener() {			
			private static final long serialVersionUID = 1L;

			@Override
			public void modifyText(ModifyEvent arg0) {
				if (getControllerListener() != null)
					((AutosuggestListControllerListener)getControllerListener())
						.searchTextModified(autoSuggestEdit.getText());
			}
		});

		Image image = new Image(getDisplay(), JournalToolbar.class.getResourceAsStream("/images/24/stop.png"));
		Label closeButton = new Label(top, SWT.NONE);
		closeButton.setSize(24, 24);
		closeButton.setImage(image);
		closeButton.addMouseListener(new MouseListener() {
			private static final long serialVersionUID = 1L;


			@Override
			public void mouseDown(MouseEvent arg0) {
				if (getControllerListener() != null)
					((AutosuggestListControllerListener)getControllerListener())
						.cancelAutosuggest();
			}

			@Override
			public void mouseUp(MouseEvent arg0) {}
			

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {}
		});

		/*
		 * Search list composite
		 */
		autoSuggestListView = new ListViewer(c, SWT.PUSH);
		autoSuggestListView.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		autoSuggestListView.setContentProvider(new CommonContentProvider());
		autoSuggestListView.setLabelProvider(labelProvider);
		autoSuggestListView.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent arg0) {				
				if (getControllerListener() != null)
					((CommonListControllerListener)getControllerListener()).itemSelected(
						(IDTO)((IStructuredSelection)arg0.getSelection()).getFirstElement()
					);
			}
		});

		return c;
	}

	public void focusOnEdit() {
		autoSuggestEdit.setFocus();	
	}
	
	public void setText(String text, int caretPosition) {
		autoSuggestEdit.setText(text);
		autoSuggestEdit.setSelection(caretPosition);
	}
	
	@Override
	protected void finalize() throws Throwable {
		displayColumnName = null;
		labelProvider = null;
		autoSuggestListView = null;
		autoSuggestEdit = null;

		super.finalize();
	}
	
	@Override
	protected CommonToolbar createToolbar() {
		return null;
	}

}
