package ru.futurelink.mo.web.composites.fields;

import org.eclipse.rap.rwt.scripting.ClientListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Text;

import ru.futurelink.mo.orm.dto.EditorDTO;
import ru.futurelink.mo.orm.dto.FilterDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.CommonItemComposite;
import ru.futurelink.mo.web.controller.CommonItemControllerListener;
import ru.futurelink.mo.web.controller.CompositeParams;

public class TextField extends CommonField {
	//protected ModifyListener 	mModifyListener;
	protected String			mInitialText;
	protected String			mRealText;
	private String				mHint;
	private String				mTextBeforeFocus;
	
	private ClientListener 		mUppercaseListener;
	private boolean			mUppercase;
	
	private static String uppercaseJS = 
			"var handleEvent = function( event ) {\n"
			+ "	event.text = event.text.toUpperCase();\n"
			+ "};\n"; 
 	
	public TextField(ApplicationSession session, CommonComposite parent, int style,
			CompositeParams params, CommonItemComposite c) {
		super(session, parent, style, params, c);
		
		createControls(style);
	}

	public TextField(ApplicationSession session, CommonComposite parent, int style,
			CompositeParams params, FilterDTO filterDTO) {
		super(session, parent, style, params, filterDTO);

		createControls(style);
	}

	public void setUppercase(boolean uppercase) {
		if (uppercase && !mUppercase) {
			mUppercaseListener = new ClientListener(uppercaseJS);
			mControl.addListener(SWT.Verify, mUppercaseListener);
			mUppercase = true;
		} else {
			mControl.removeListener(SWT.Verify, mUppercaseListener);
			mUppercase = false;
		}
	}
	
	protected void createControls(int style) {
		mHint = new String();
		mRealText = new String();

		mControl = new Text(mParent, SWT.BORDER | (style & SWT.READ_ONLY) | (style & SWT.MULTI));
		setTextLimit(255);	// По-умолчанию ограничение 255 символов

		/*mModifyListener = new ModifyListener() {			
			private static final long serialVersionUID = 1L;
			@Override
			public void modifyText(ModifyEvent arg0) {
				try {
					if (storeText()) {
						if (mFieldModifyListener != null) {
							mFieldModifyListener.modifyText(arg0);
						}						
					}

					if (getControllerListener() != null)
						((CommonItemControllerListener)getControllerListener()).dataChanged(getSelf());
				} catch (DTOException ex) {
					getControllerListener().sendError("Ошибка обновления текстового поля!", ex);
				}
			}
		};
		((Text)mControl).addModifyListener(mModifyListener);*/

		mControl.addFocusListener(new FocusListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void focusLost(FocusEvent arg0) {
				try {
					// Это событие должно происходить при потере фокуса в том случае
					// если данные реально менялись. Сохраняем текст еще раз, так как мы не
					// уверены, что событие modifyText действительно успело сгерериться
					// до потери фокуса. RAP немного оттормаживается при быстром изменении
					// поля ввода.
					storeText();
				
					// Если в данных пусто - покажем подсказку
					if ((getText() == null) || getText().equals("")) {
						((Text)mControl).setText(getHint());
					}

					if (
						(getControllerListener() != null) && 
						(!getText().equals(mTextBeforeFocus))
					) {
						((CommonItemControllerListener)getControllerListener()).dataChangeFinished(getSelf());
					}
				} catch (DTOException ex) {
					getControllerListener().sendError("Ошибка обновления текстового поля!", ex);
				}
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
				mInitialText = String.copyValueOf(getText().toCharArray());
				mTextBeforeFocus = getText();

				// Сбросим подсказку, если получили фокус, и текст равер подсказке
				if (((Text)mControl).getText().equals(getHint())) {
					((Text)mControl).setText(getText());
				}
			}
		});				
	}
	
	/**
	 * Сохранить содержимое ввода в DTO.
	 * 
	 * @return
	 * @throws DTOException
	 */
	protected boolean storeText() throws DTOException {
		boolean isModified = false;

		// Если текст изменился на подсказку - то считаем что текст пустой
		if (((Text)mControl).getText().equals(getHint())) {
			mRealText = new String();
		} else {
			mRealText = String.copyValueOf(((Text)mControl).getText().toCharArray());
		}
		
		// Вызываем этот метод только для DTO предназначенных для редакитрования					
		if ((getDTO() != null) && 
				(EditorDTO.class.isAssignableFrom(getDTO().getClass()) || 
				 FilterDTO.class.isAssignableFrom(getDTO().getClass()))
			) {
			
			// Это касается фильтра
			if (FilterDTO.class.isAssignableFrom(getDTO().getClass()) && getUseOnlyOneCondition())
				getDTO().setDataField(mDataFieldName, mDataFieldGetter, mDataFieldSetter, null);

			// Если поле используется для DTO фильтра, то пустое значение = null, иначе,
			// пустое значение - это пустая строка.
			if ((getText() == null) || getText().equals(getHint())) {
				if (FilterDTO.class.isAssignableFrom(getDTO().getClass()))
					getDTO().setDataField(mDataFieldName, mDataFieldGetter, mDataFieldSetter, null);
				else
					getDTO().setDataField(mDataFieldName, mDataFieldGetter, mDataFieldSetter, getValue());
			} else {
				getDTO().setDataField(mDataFieldName, mDataFieldGetter, mDataFieldSetter, getValue());
			}
			
			isModified = true;
		}
		
		handleMandatory();
		
		return isModified;
	}
	
	/**
	 * Установить значение поля.
	 * 
	 * @param text
	 * @throws DTOException
	 */
	public void setText(String text) throws DTOException {
		if ((text == null) || text.equals("")) {
			((Text)mControl).setText(getHint());
			mRealText = text;
		} else {
			((Text)mControl).setText(text);
			((Text)mControl).setSelection(text.length());	// Переместим курсор в конец
			mRealText = String.copyValueOf(text.toCharArray());
		}
		
		handleMandatory();
	}

	public String getText() {
		return mRealText;
	}

	public void setTextLimit(int limit) {
		((Text)mControl).setTextLimit(limit);
	}
	
	@Override
	public void setEditable(boolean isEditable) {
		if (((Text)mControl).getEditable() != isEditable) {
			((Text)mControl).setEditable(isEditable);
		}
	}
	
	public void setForeground(Color color) {
		mControl.setForeground(color);
	}
	
	public void setBackground(Color color) {
		mControl.setBackground(color);
	}

	@Override
	public void refresh() throws DTOException {
		Object f = null;
		if (getDTO() != null)
			f = getDTO().getDataField(mDataFieldName, mDataFieldGetter, mDataFieldSetter);
		setText(f != null ? f.toString() : "");
	}

	/**
	 * Установить символ, который маскирует вводимые значения.
	 * 
	 * @param character
	 */
	public void setEchoChar(char character) {
		((Text)mControl).setEchoChar(character);
	}
	
	/**
	 * Установить подсказку поля.
	 * 
	 * @param hintText
	 */
	public void setHint(String hintText) {
		mHint = hintText;
		if ((getText() == null) || getText().equals(""))
			((Text)mControl).setText(mHint);
	}
	
	/**
	 * Возвращает подсказку поля.
	 * 
	 * @return
	 */
	public String getHint() {
		return mHint;
	}	
	
	/**
	 * Изменены ли данные в поле с момента последнего обращения к нему?
	 *  
	 * @return
	 */
	public boolean getIsDataChanged() {
		return !getText().equals(mInitialText);
	}

	@Override
	public boolean isEmpty() {
		return (getText()==null)||getText().isEmpty();
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
		return getText();
	}

}
