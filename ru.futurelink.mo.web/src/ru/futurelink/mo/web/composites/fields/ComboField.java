package ru.futurelink.mo.web.composites.fields;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Event;

import ru.futurelink.mo.orm.dto.EditorDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.CommonItemComposite;
import ru.futurelink.mo.web.controller.CommonItemControllerListener;
import ru.futurelink.mo.web.controller.CompositeParams;

/**
 * Поле комбо-выбора, для использования внутри фреймворка NO.
 * 
 * - dataChanged event is called on controller if data was changed in data field, no matter what
 * caused that update,
 * - dataChangeFinished is called on controller if data was changed through GUI, but not programmatically.  
 * 
 * @author pavlov
 *
 */
public class ComboField extends CommonField {
	private SelectionListener	mSelectionListener;
	private int				mDefaultIndex = -1;

	private Map<String, String>		mSource;
	
	public ComboField(ApplicationSession session, CommonComposite parent,
			int style, CompositeParams params,
			CommonItemComposite dataComposite) {
		super(session, parent, style, params, dataComposite);

		mControl = new Combo(mParent, SWT.BORDER | SWT.READ_ONLY);
		mSelectionListener = new SelectionListener() {			
			private static final long serialVersionUID = 1L;
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {}
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					storeSelection();
					
					if (mFieldModifyListener != null) {
						Event event = new Event();
						ModifyEvent e = new ModifyEvent(event);
						mFieldModifyListener.modifyText(e);
					}			
					
					if (getControllerListener() != null)
						((CommonItemControllerListener)getControllerListener()).dataChangeFinished(getSelf());
				} catch (DTOException ex) {
					getControllerListener().sendError("Ошибка обновления элмента выбора!", ex);
				}				
			}
		};
		((Combo)mControl).addSelectionListener(mSelectionListener);
	}

	/**
	 * Сохранить выбранный элемент.
	 * 
	 * @throws DTOException
	 */
	private void storeSelection() throws DTOException {
		if ((getDTO() != null) && (EditorDTO.class.isAssignableFrom(getDTO().getClass()))) {
			getDTO().setDataField(mDataFieldName, mDataFieldGetter, mDataFieldSetter, 
					getSelection());

			if (getControllerListener() != null)
				((CommonItemControllerListener)getControllerListener()).dataChanged(getSelf());			
		}
		
		handleMandatory();		
	}
	
	public void select(Integer index) {
		((Combo)mControl).select(index);

		try {
			storeSelection();
		} catch (DTOException e) {
			e.printStackTrace();
		}
	}

	public void setDefaultIndex(int index) {
		mDefaultIndex = index;
	}
	
	public void setSource(Map<String, String> source) {
		mSource = source;
		fillList();
	}

	@Override
	public void refresh() throws DTOException {
		if (getDTO() != null) {
			Object f = getDTO().getDataField(mDataFieldName, mDataFieldGetter, mDataFieldSetter);
			if (f != null) {
				int index = 0;
				// Пробежимся по набору ключей источника и вытащим индекс,
				// считаем, что если список заполнялся в этом порядке, то
				// порядок не должен быть нарушен.
				for (String s : mSource.keySet()) {
					if (s.equals(f)) select(index);
					index++;
				}
			} else {
				// Проставляем значение по-умолчанию для поля выбора
				if (mDefaultIndex >= 0) {
					select(mDefaultIndex);
				}
			}
		}

		handleMandatory();
	}

	/**
	 * Заполнить список значениями из источника. 
	 */
	private void fillList() {
		for (String s : mSource.keySet()) {
			((Combo)mControl).add(mSource.get(s));
		}		
	}
	
	private String getSelection() {
		int index = ((Combo)mControl).getSelectionIndex();
		String value = ((Combo)mControl).getItem(index);

		// найдем значение в источнике
		for (String key : mSource.keySet()) {
			if ((mSource.get(key) != null) && mSource.get(key).equals(value))
				return key;
		}
		return null;
	}

	@Override
	public boolean isEmpty() {
		return (((Combo)mControl).getSelectionIndex() < 0);
	}

	@Override
	public void handleMandatory() {
		if (getMandatory()&&isEmpty()) {
			mControl.setBackground(new Color(mControl.getDisplay(), 255, 169, 169));
		} else {
			mControl.setBackground(new Color(mControl.getDisplay(), 255, 255, 255));
		}
	}

	@Override
	public Object getValue() {
		return getSelection();
	}	
}
