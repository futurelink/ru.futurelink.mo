package ru.futurelink.mo.web.composites.fields;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.CommonItemComposite;
import ru.futurelink.mo.web.controller.CompositeParams;

/**
 * Кнопка с написанным на ней текстом, который берется из DTO.
 * 
 * @author pavlov
 *
 */
public class TextButton extends CommonField {

	private SelectionListener mSelectionListener;
	
	public TextButton(ApplicationSession session, CommonComposite parent,
			int style, CompositeParams params,
			CommonItemComposite dataComposite) {
		super(session, parent, style, params, dataComposite);

		mControl = new Button(mParent, SWT.PUSH);
		((Button)mControl).addSelectionListener(new SelectionListener() {		
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (mSelectionListener != null) {
					mSelectionListener.widgetSelected(arg0);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				if (mSelectionListener != null) {
					mSelectionListener.widgetDefaultSelected(arg0);
				}				
			}
		});
	}

	public String getText() {
		return ((Text)mControl).getText();
	}
	
	public void setText(String text) throws DTOException {
		if (!((Button)mControl).getText().equals(text)) {
			((Button)mControl).setText(text);
		}
	}

	@Override
	public void refresh() throws DTOException {
		Object f = getDTO().getDataField(mDataFieldName, mDataFieldGetter, mDataFieldSetter);
		setText(f != null ? f.toString() : "");
	}

	public void addSelectionListener(SelectionListener listener) {
		mSelectionListener = listener;
	}

	@Override
	public boolean isEmpty() {
		return (getDTO() == null);
	}

	@Override
	protected void handleMandatory() {
	
	}

	@Override
	public Object getValue() {
		return getDTO();
	}

}
