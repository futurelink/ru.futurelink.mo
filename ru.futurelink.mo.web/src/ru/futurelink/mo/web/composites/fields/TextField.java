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

import java.util.List;

import org.eclipse.rap.rwt.scripting.ClientListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;

import ru.futurelink.mo.orm.dto.EditorDTO;
import ru.futurelink.mo.orm.dto.FilterDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.CommonItemComposite;
import ru.futurelink.mo.web.controller.CommonItemControllerListener;
import ru.futurelink.mo.web.controller.CompositeParams;

/**
 * Common text input field.
 * 
 * @author pavlov
 *
 */
public class TextField extends CommonField {
	//protected ModifyListener 	mModifyListener;
	protected String			mInitialText;
	protected String			mRealText;
	private String				mHint;
	private String				mTextBeforeFocus;
	
	private ClientListener 		mValidateListener;
	private boolean			mUppercase;	
	private boolean			mDisallowSpaces;

	private List<String>		mSource;

	private boolean			mStopOnError;
	
	public static int			COMBO = 0b10000000;
	public int					mStyle;

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

	private final String generateValidator() {
		StringBuilder validator = new StringBuilder();
		validator.append("var handleEvent = function( event ) {\n");
		
		if (mDisallowSpaces)
			validator.append("	if (event.text == ' ') event.doit = false;\n");

		if (mUppercase)
			validator.append("	event.text = event.text.toUpperCase();\n");
		
		validator.append("};\n"); 

		return validator.toString();
	}
	
	private final void setValidator() {
		if (mValidateListener != null)
			mControl.removeListener(SWT.Verify, mValidateListener);	

		mValidateListener = new ClientListener(generateValidator());
		mControl.addListener(SWT.Verify, mValidateListener);		
	}
	
	/**
	 * Force uppercase for user's input.
	 * 
	 * @param uppercase
	 */
	public void setUppercase(boolean uppercase) {
		if (uppercase && !mUppercase) {
			mUppercase = true;
		} else {
			mUppercase = false;
		}
		setValidator();
	}
	
	/**
	 * Disallow spaces for user's input (force one word).
	 * 
	 * @param disallowSpaces
	 */
	public void setDisallowSpaces(boolean disallowSpaces) {
		if (disallowSpaces && !mDisallowSpaces) {
			mDisallowSpaces = true;
		} else {
			mDisallowSpaces = false;
		}
		setValidator();
	}
	
	protected void createControls(int style) {
		mHint = new String();
		mRealText = new String();

		mStyle = style;

		if ((style & TextField.COMBO) == TextField.COMBO) {
			mControl = new Combo(mParent, SWT.BORDER);
		} else {
			mControl = new Text(mParent, SWT.BORDER | (style & SWT.READ_ONLY) | (style & SWT.MULTI));

			((Text)mControl).addModifyListener(new ModifyListener() {			
				private static final long serialVersionUID = 1L;

				@Override
				public void modifyText(ModifyEvent arg0) {
					if (mFieldModifyListener != null)
						mFieldModifyListener.modifyText(arg0);				
				}
			});
		}
		setTextLimit(255);	// По-умолчанию ограничение 255 символов
		
		mControl.addFocusListener(new FocusListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void focusLost(FocusEvent arg0) {
				
				// Do not propagate focus lost event because of exception,
				// even if message box appeared and focus lost again.
				if (mStopOnError) return;
				
				try {
					// Это событие должно происходить при потере фокуса в том случае
					// если данные реально менялись. Сохраняем текст еще раз, так как мы не
					// уверены, что событие modifyText действительно успело сгерериться
					// до потери фокуса. RAP немного оттормаживается при быстром изменении
					// поля ввода.
					storeText();
				
					// Если в данных пусто - покажем подсказку
					if ((mStyle & TextField.COMBO) != TextField.COMBO) {
						if ((getText() == null) || getText().equals("")) {
							((Text)mControl).setText(getHint());
						}
					}

					if (
						(getControllerListener() != null) && 
						(!getText().equals(mTextBeforeFocus))
					) {
						((CommonItemControllerListener)getControllerListener()).dataChangeFinished(getSelf());
					}
				} catch (DTOException ex) {
					mStopOnError = true;
					getControllerListener().sendError("Ошибка обновления текстового поля!", ex);
				}
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
				mInitialText = String.copyValueOf(getText().toCharArray());
				mTextBeforeFocus = getText();

				// Сбросим подсказку, если получили фокус, и текст равер подсказке
				if ((mStyle & TextField.COMBO) != TextField.COMBO) {
					if (((Text)mControl).getText().equals(getHint())) {
						((Text)mControl).setText(getText());
					}
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
		if ((mStyle & TextField.COMBO) != TextField.COMBO) {
			if (((Text)mControl).getText().equals(getHint())) {
				mRealText = new String();
			} else {
				mRealText = String.copyValueOf(((Text)mControl).getText().toCharArray());
			}
		} else {
			mRealText = String.copyValueOf(((Combo)mControl).getText().toCharArray());
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

			if (getControllerListener() != null)
				((CommonItemControllerListener)getControllerListener()).dataChanged(getSelf());
		}
		
		handleMandatory();
		
		return isModified;
	}
	
	/**
	 * Set field text value.
	 * 
	 * @param text
	 * @throws DTOException
	 */
	public void setText(String text) throws DTOException {
		if ((text == null) || text.equals("")) {
			if ((mStyle & TextField.COMBO) != TextField.COMBO) {
				((Text)mControl).setText(getHint());
				mRealText = text;
			}
		} else {
			if ((mStyle & TextField.COMBO) != TextField.COMBO) {
				((Text)mControl).setText(text);
				((Text)mControl).setSelection(text.length());	// Переместим курсор в конец
			} else {
				((Combo)mControl).setText(text);			
			}
			mRealText = String.copyValueOf(text.toCharArray());
		}
		
		handleMandatory();
	}

	/**
	 * Get field text value.
	 * 
	 * @return
	 */
	public String getText() {
		return mRealText;
	}

	/**
	 * Set maximum text length for field.
	 * 
	 * @param limit
	 */
	public void setTextLimit(int limit) {
		if ((mStyle & TextField.COMBO) == TextField.COMBO) {
			((Combo)mControl).setTextLimit(limit);
		} else {
			((Text)mControl).setTextLimit(limit);
		}
	}
	
	/**
	 * Set text field editable.
	 */
	@Override
	public void setEditable(boolean isEditable) {
		if ((mStyle & TextField.COMBO) == TextField.COMBO) {
			
		} else {
			if (((Text)mControl).getEditable() != isEditable) {
				((Text)mControl).setEditable(isEditable);
			}			
		}
	}
	
	/**
	 * Set field foreground color.
	 * 
	 * @param color
	 */
	public void setForeground(Color color) {
		mControl.setForeground(color);
	}
	
	/**
	 * Set field background color.
	 * 
	 * @param color
	 */
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
	 * Set field echo character, ex. for password chars.
	 * 
	 * @param character
	 */
	public void setEchoChar(char character) {
		((Text)mControl).setEchoChar(character);
	}
	
	/**
	 * Set field hint, displayed inside text input box.
	 * 
	 * @param hintText
	 */
	public void setHint(String hintText) {
		mHint = hintText;
		if ((mStyle & TextField.COMBO) == TextField.COMBO) {
			
		} else {
			if ((getText() == null) || getText().equals(""))
				((Text)mControl).setText(mHint);
		}
	}
	
	/**
	 * Get field hint.
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

	public void setSource(List<String> sourceList) {
		mSource = sourceList;
		if ((mStyle & TextField.COMBO) == TextField.COMBO) {
			for (String s : mSource) {
				((Combo)mControl).add(s);
			}
		}
	}
	
}
