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

package ru.futurelink.mo.web.composites.fields;

import java.util.Calendar;
import java.util.TimeZone;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.types.DateRange;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.CommonItemComposite;
import ru.futurelink.mo.web.controller.CommonItemControllerListener;
import ru.futurelink.mo.web.controller.CompositeParams;

/**
 * Month and year selection field, without a day selector.
 * 
 * @author pavlov
 *
 */
public class MonthSelectionField extends CommonField {
	private Combo mMonthSelection;
	private Combo mYearSelection;
	
	private String[] mMonths = {
			"monthJanuary", "monthFebruary", "monthMarch", "monthApril",
			"monthMay", "monthJune", "monthJuly", "monthAugust",
			"monthSeptember", "monthOctober", "monthNovember", "monthDecember"};

	private SelectionListener		mSelectionListener;
	
	private boolean				mUseDateRange;
		
	public MonthSelectionField(ApplicationSession session,
			CommonComposite parent, int style, CompositeParams params,
			CommonItemComposite dataComposite) {
		super(session, parent, style, params, dataComposite);
		
		createControls();
	}

	public MonthSelectionField(ApplicationSession session,
			CommonComposite parent, int style, CompositeParams params,
			CommonDTO dto) {
		super(session, parent, style, params, dto);
		
		createControls();
	}	

	private void createControls() {		
		mControl = new CommonComposite(mParent.getSession(), mParent, SWT.NONE, null);

		GridLayout gl = new GridLayout(2, true);
		((CommonComposite)mControl).setLayout(gl);
		
		mMonthSelection = new Combo(((CommonComposite)mControl), SWT.BORDER | SWT.READ_ONLY);
		mMonthSelection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		for (int i = 0; i < 12; i++) {
			mMonthSelection.add(mParent.getLocaleString(mMonths[i]));
		}
		mMonthSelection.addSelectionListener(new SelectionListener() {		
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				fillDataBySelectionEvent();

				if (mSelectionListener != null)
					mSelectionListener.widgetSelected(arg0);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {}
		});

		mYearSelection = new Combo(((CommonComposite)mControl), SWT.BORDER | SWT.READ_ONLY);
		mYearSelection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		mYearSelection.addSelectionListener(new SelectionListener() {		
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				fillDataBySelectionEvent();

				if (mSelectionListener != null)
					mSelectionListener.widgetSelected(arg0);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {}
		});

		mYearSelection.add("2013");
		mYearSelection.add("2014");
	}
	
	public void setUseDateRange(boolean useDateRange) {
		mUseDateRange = useDateRange;
	}
		
	private void fillDataBySelectionEvent() {
		try {					
			// Если у нас используется только одно возможное условие в фильтре,
			// например, для быстрого фильтра на панели инструментов, нужно очищать
			// поле фильтрации, чтобы использовать только одно условие.
			if (getUseOnlyOneCondition())
				getDTO().setDataField(mDataFieldName, mDataFieldGetter, mDataFieldSetter, null);

			if (mUseDateRange) {
				getDTO().setDataField(mDataFieldName, mDataFieldGetter, mDataFieldSetter, 
					formatDateRange());
			} else {
				getDTO().setDataField(mDataFieldName, mDataFieldGetter, mDataFieldSetter, 
					formatDate());
			}

			// Передаем изменение данных, если привязан обработчик.
			if (getControllerListener() != null)
				((CommonItemControllerListener)getControllerListener()).dataChanged(null);
			
		} catch (DTOException ex) {
			getControllerListener().sendError("Ошибка обновления даты-месяца!", ex);
		}						
	}
	
	/**
	 * Формирует массив из двух чисел - года и месяца.
	 * 
	 * @return
	 */
	private Integer[] formatDate() {
		Integer[] dt = new Integer[2];
		dt[0] = getYear();
		dt[1] = getMonth();
		return dt;
	}
	
	/**
	 * Сформировать DateRange из двух дат - первого дня месяца и последнего дня месяца.
	 * 
	 * Date range is formed from dates calculated on local server timezone and user timezone.
	 * 
	 * <b>For example:</b> 
	 * 1) When user selects october and sever has GMT+10 time zone and user has GMT+12 time zone 
	 * the result will be 01 Oct 02:00:00 till 01 Nov 01:59:59.
	 * 2) If server is on UTC and user is GMT-2 the result will be 30 Sep 22:00:00 till 30 Oct 21:59:59.
	 * 
	 * @return
	 */
	private DateRange formatDateRange() {
		DateRange range = new DateRange();
		
		Calendar c = Calendar.getInstance();
		c.set(getYear(), getMonth()-1, 1, 0, 0, 0);
		c.add(Calendar.MILLISECOND, 
				TimeZone.getDefault().getRawOffset() - 
				mParent.getSession().getUser().getTimeZone().getRawOffset());
		range.setBeginDate(c.getTime());

		Integer lastDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);

		c.set(getYear(), getMonth()-1, lastDay, 23, 59, 59);
		c.add(Calendar.MILLISECOND, 
				TimeZone.getDefault().getRawOffset() - 
				mParent.getSession().getUser().getTimeZone().getRawOffset());
		range.setEndDate(c.getTime());

		System.out.println("DateRange set to "+range.getBeginDate().toString()+" to "+range.getEndDate().toString());
		
		return range;
	}
	
	/**
	 * Установить выбранную дату из массива двух элментов: года, месяца.
	 * 
	 * @param date
	 */
	public void setFormatDate(Integer[] date) {
		setSelection(date[1], date[0]);
	}
	
	/**
	 * Установить выбранную дату из форматированной строки "год,месяц".
	 * 
	 * @param dateString
	 */
	public void setFormatDate(String dateString) {
		String[] strs = dateString.split(",");
		if (strs.length == 2) {
			setSelection(Integer.valueOf(strs[1]), Integer.valueOf(strs[0]));
		}
	}
	
	/**
	 * Выбрать месяц-год на селекторе.
	 * 
	 * @param month
	 * @param year
	 */
	public void setSelection(int month, int year) {
		if ((month >= 1) && (month <= 12))
			mMonthSelection.select(month-1);
		else 
			mMonthSelection.select(0);
		
		for (int i = 0; i < mYearSelection.getItems().length; i++) {
			if (mYearSelection.getItems()[i].equals(String.valueOf(year))) {
				mYearSelection.select(i);
			}
		}

		fillDataBySelectionEvent();
	}

	public Integer getYear() {
		if (mYearSelection.getSelectionIndex() >= 0) {
			return Integer.valueOf(mYearSelection.getItem(mYearSelection.getSelectionIndex()));
		} else {
			return 0;
		}
	}
	
	
	public Integer getMonth() {
		return mMonthSelection.getSelectionIndex()+1;
	}
	
	public void addSelectionListener(SelectionListener listener) {
		mSelectionListener = listener;
	}

	@Override
	public void setEditable(boolean isEditable) {
		mControl.setEnabled(isEditable);
	}

	@Override
	public void refresh() {
		
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
		return null;
	}

}
