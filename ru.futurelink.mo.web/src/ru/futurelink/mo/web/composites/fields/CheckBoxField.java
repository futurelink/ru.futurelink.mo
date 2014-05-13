package ru.futurelink.mo.web.composites.fields;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.EditorDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.CommonItemComposite;
import ru.futurelink.mo.web.controller.CommonItemControllerListener;
import ru.futurelink.mo.web.controller.CompositeParams;

public class CheckBoxField extends CommonField {
	public CheckBoxField(ApplicationSession session, CommonComposite parent,
			int style, CompositeParams params,
			CommonItemComposite dataComposite) {
		super(session, parent, style, params, dataComposite);
		
		createField();
	}

	public CheckBoxField(ApplicationSession session, CommonComposite parent,
			int style, CompositeParams params,
			CommonDTO dto) {
		super(session, parent, style, params, dto);
		
		createField();
	}

	private void createField() {
		mControl = new Button(mParent, SWT.CHECK);
		((Button)mControl).addSelectionListener(new SelectionListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					if (EditorDTO.class.isAssignableFrom(getDTO().getClass())) {
						getDTO().setDataField(mDataFieldName, mDataFieldGetter, mDataFieldSetter, ((Button)mControl).getSelection());
					}
					
					if (getControllerListener() != null) {
						((CommonItemControllerListener)getControllerListener()).dataChanged(getSelf());
						((CommonItemControllerListener)getControllerListener()).dataChangeFinished(getSelf());
					}
				} catch (DTOException ex) {
					getControllerListener().sendError("Ошибка обновления элмента чекбокса!", ex);
				} 
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {}
		});
	}
	
	public void setText(String text) {
		((Button)mControl).setText(text);
	}

	@Override
	public void refresh() throws DTOException {
		boolean selected = false;
		if (getDTO().getDataField(mDataFieldName, mDataFieldGetter, mDataFieldSetter) != null) {
			selected = (Boolean)getDTO().getDataField(mDataFieldName, mDataFieldGetter, mDataFieldSetter);
		}
		((Button)mControl).setSelection(selected);
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	protected void handleMandatory() {}

	@Override
	public Object getValue() {
		return ((Button)mControl).getSelection();
	}

}
