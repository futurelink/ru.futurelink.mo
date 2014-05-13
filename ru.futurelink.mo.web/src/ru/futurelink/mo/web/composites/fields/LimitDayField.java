package ru.futurelink.mo.web.composites.fields;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.types.LimitDay;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.CommonItemComposite;
import ru.futurelink.mo.web.controller.CompositeParams;
import ru.futurelink.mo.web.controller.CommonItemControllerListener;

public class LimitDayField extends CommonField {
	private static final String	weekDayCounter[] = {"First", "Second", "Third", "Forth", "Last"};
	private static final String weekDays[] = {"Monday", "Tuesday", "Wensday", "Thursday", "Friday", "Saturday", "Sunday"};
	
	private LimitDay mLimitDay;
	
	private Button b1;
	private Button b2;
	private Combo cmb;
	private Combo cmb2;
	private Combo cmb3;
	
	public LimitDayField(ApplicationSession session, CommonComposite parent,
			int style, CompositeParams params,
			CommonItemComposite dataComposite) {
		super(session, parent, style | SWT.BORDER, params, dataComposite);
		
		mLimitDay = new LimitDay();
		mControl = new CommonComposite(mParent.getSession(), mParent, SWT.BORDER | SWT.READ_ONLY, null);

		GridLayout layout = new GridLayout(2, false);
		((CommonComposite)mControl).setLayout(layout);
		
		b1 = new Button(((CommonComposite)mControl), SWT.RADIO);
		b1.setData("dayOfMonth");
		b1.setText(((CommonComposite)mControl).getLocaleString("limitTypeByDayOfMonth"));
		b1.addSelectionListener(typeSelectionListener);

		b2 = new Button(((CommonComposite)mControl), SWT.RADIO);
		b2.setData("dayOfWeek");
		b2.setText(((CommonComposite)mControl).getLocaleString("limitTypeByDayOfWeek"));
		b2.addSelectionListener(typeSelectionListener);

		Label l = new Label(((CommonComposite)mControl), SWT.NONE);
		l.setText(((CommonComposite)mControl).getLocaleString("limitTypeDayOfMonth"));

		cmb = new Combo(((CommonComposite)mControl), SWT.BORDER | SWT.READ_ONLY);
		cmb.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		for (int i = 1; i < 32; i++) {
			cmb.add(String.valueOf(i));
		}
		cmb.addSelectionListener(mDataSelectionListener);
		
		Label l2 = new Label(((CommonComposite)mControl), SWT.NONE);
		l2.setText(((CommonComposite)mControl).getLocaleString("limitTypeDayOfWeek"));
		
		Composite c = new Composite(((CommonComposite)mControl), SWT.NONE);
		GridLayout sgd =new GridLayout(2, true);
		sgd.marginWidth = 0;
		sgd.marginHeight = 0;
		c.setLayout(sgd);
		c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

		cmb2 = new Combo(c, SWT.BORDER | SWT.READ_ONLY);
		cmb2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		cmb2.add(((CommonComposite)mControl).getLocaleString("counterFirst"));
		cmb2.add(((CommonComposite)mControl).getLocaleString("counterSecond"));
		cmb2.add(((CommonComposite)mControl).getLocaleString("counterThird"));
		cmb2.add(((CommonComposite)mControl).getLocaleString("counterForth"));
		cmb2.add(((CommonComposite)mControl).getLocaleString("counterLast"));
		cmb2.addSelectionListener(mDataSelectionListener);
							
		cmb3 = new Combo(c, SWT.BORDER | SWT.READ_ONLY);
		cmb3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		for (int i = 0; i < 7; i++) {
			cmb3.add(((CommonComposite)mControl).getLocaleString("day"+weekDays[i]));
		}
		cmb3.addSelectionListener(mDataSelectionListener);

		b1.setSelection(true);
		//typeSelectionListener.widgetSelected(new SelectionEvent(b1, null, 0));
	}

	private SelectionListener mDataSelectionListener = new SelectionListener() {
		private static final long serialVersionUID = 1L;

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			try {
				getDTO().setDataField(mDataFieldName, mDataFieldGetter, mDataFieldSetter, getLimitDay());
				((CommonItemControllerListener)getControllerListener()).dataChanged(null);
			} catch (DTOException ex) {
				getControllerListener().sendError("Ошибка изменения типа пограничного дня.", ex);
			}						
		}
		
		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {}
	};
	
	private SelectionListener typeSelectionListener = new SelectionListener() {	
		private static final long serialVersionUID = 1L;

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			if (((Control)arg0.getSource()).getData().toString().equals("dayOfMonth")) {
				cmb.setEnabled(true);
				cmb2.setEnabled(false);
				cmb3.setEnabled(false);
			} else {
				cmb.setEnabled(false);
				cmb2.setEnabled(true);
				cmb3.setEnabled(true);				
			}
			try {
				if (getControllerListener() != null)
					((CommonItemControllerListener)getControllerListener()).dataChanged(null);
			} catch (DTOException ex) {
				getControllerListener().sendError("Ошибка изменения типа пограничного дня.", ex);
			}			
		}
		
		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {}
	};
	
	public void setLimitDay(LimitDay day) throws DTOException {
		mLimitDay = day;

		if (day == null) {
			mLimitDay = new LimitDay();
			
			b1.setSelection(true);
			cmb.select(-1);
			cmb2.select(-1);
			cmb3.select(-1);
		} else {
			if (day.getLimitType() == 0) {
				b1.setSelection(true);
				b2.setSelection(false);
				//typeSelectionListener.widgetSelected(new SelectionEvent(b1, null, 0));
			} else if(day.getLimitType() == 1) {
				b1.setSelection(false);
				b2.setSelection(true);
				//typeSelectionListener.widgetSelected(new SelectionEvent(b2, null, 0));
			}
		
			if (day.getMonthDay() > 0) cmb.select(day.getMonthDay()-1);		
			if (day.getWeekDayNumber() > 0) cmb2.select(day.getWeekDayNumber()-1);
			if (day.getWeekDay() > 0) cmb3.select(day.getWeekDay()-1);
		}
		
		((CommonItemControllerListener)getControllerListener()).dataChanged(null);		
	}
	
	public LimitDay getLimitDay() {
		if (b1.getSelection())
			mLimitDay.setLimitType(0);
		else
			mLimitDay.setLimitType(1);
		
		if (cmb.getSelectionIndex() >= 0) { mLimitDay.setMonthDay(cmb.getSelectionIndex()+1); }
		if (cmb2.getSelectionIndex() >= 0) { mLimitDay.setWeekDayNumber(cmb2.getSelectionIndex()+1); }
		if (cmb3.getSelectionIndex() >= 0) { mLimitDay.setWeekDay(cmb3.getSelectionIndex()+1); }
				
		return mLimitDay;
	}

	public static String getDayName(int weekDay) {
		return weekDays[weekDay-1];
	}

	public static String getDayCounter(int weekDayNumber) {
		return weekDayCounter[weekDayNumber-1];
	}

	@Override
	public void setEditable(boolean isEditable) {
		((CommonComposite)mControl).setEnabled(isEditable);
	}

	@Override
	public void refresh() throws DTOException {
		Object f = getDTO().getDataField(mDataFieldName, mDataFieldGetter, mDataFieldSetter);
		setLimitDay((LimitDay)f);
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	protected void handleMandatory() {
		
	}

	@Override
	public Object getValue() {
		return null;
	}

}
