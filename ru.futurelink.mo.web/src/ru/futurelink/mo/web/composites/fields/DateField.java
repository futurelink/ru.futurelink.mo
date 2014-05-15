package ru.futurelink.mo.web.composites.fields;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.DateTime;

import ru.futurelink.mo.orm.dto.EditorDTO;
import ru.futurelink.mo.orm.dto.FilterDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.CommonItemComposite;
import ru.futurelink.mo.web.controller.CommonItemControllerListener;
import ru.futurelink.mo.web.controller.CompositeParams;

public class DateField extends CommonField {
	public DateField(ApplicationSession session, CommonComposite parent, int style,
			CompositeParams params, CommonItemComposite c) {
		super(session, parent, style, params, c);

		createControls(style);
	}
	
	public DateField(ApplicationSession session, CommonComposite parent, int style,
			CompositeParams params, FilterDTO filterDTO) {
		super(session, parent, style, params, filterDTO);

		createControls(style);
	}

	protected void createControls(int style) {
		mControl = new DateTime(mParent, SWT.DROP_DOWN | SWT.BORDER | (style & SWT.READ_ONLY));
		((DateTime)mControl).addSelectionListener(new SelectionListener() {			
			private static final long serialVersionUID = 1L;
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {	}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					Calendar c = Calendar.getInstance(mParent.getSession().getUser().getTimeZone());
					c.set(  ((DateTime)mControl).getYear(), 
							((DateTime)mControl).getMonth(), 
							((DateTime)mControl).getDay());
					if (EditorDTO.class.isAssignableFrom(getDTO().getClass())) {
						getDTO().setDataField(mDataFieldName, mDataFieldGetter, mDataFieldSetter, c.getTime());
					}

					if (getControllerListener() != null)
						((CommonItemControllerListener)getControllerListener()).dataChanged(null);
				} catch (DTOException ex) {
					getControllerListener().sendError("Ошибка обновления даты!", ex);
				}				
			}
		});		
	}
	
	public void setDate(Date date) throws DTOException {
		if (date != null) {
			Calendar c = Calendar.getInstance(mParent.getSession().getUser().getTimeZone());
			c.setTime(date);
			((DateTime)mControl).setDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		}
		if (getControllerListener() != null)
			((CommonItemControllerListener)getControllerListener()).dataChanged(null);
	}
	
	public Date getDate() {
		Calendar c = Calendar.getInstance(mParent.getSession().getUser().getTimeZone());
		c.set(getYear(), getMonth(), getDay(), 
			((DateTime)mControl).getHours(), ((DateTime)mControl).getMinutes(), ((DateTime)mControl).getSeconds());
		return c.getTime();
	}
	
	public Integer getMonth() {
		return ((DateTime)mControl).getMonth();
	}

	public Integer getDay() {
		return ((DateTime)mControl).getDay();
	}

	public Integer getYear() {
		return ((DateTime)mControl).getYear();
	}

	@Override
	public void refresh() throws DTOException {
		if (getDTO() != null) {
			Object f = getDTO().getDataField(mDataFieldName, mDataFieldGetter, mDataFieldSetter);
			if (f != null) {
				setDate((Date)f);
			} else {
				getDTO().setDataField(mDataFieldName, mDataFieldGetter, mDataFieldSetter, getDate());
			}
		}
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public void handleMandatory() {
		
	}

	@Override
	public Object getValue() {
		return getDate();
	}
	
}
