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

package ru.futurelink.mo.web.composites.fields.datapicker;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.FilterDTO;
import ru.futurelink.mo.orm.dto.IDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.iface.ICommonObject;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.CommonItemComposite;
import ru.futurelink.mo.web.composites.dialogs.CommonDialog;
import ru.futurelink.mo.web.composites.fields.CommonField;
import ru.futurelink.mo.web.controller.CommonItemController;
import ru.futurelink.mo.web.controller.CommonItemControllerListener;
import ru.futurelink.mo.web.controller.CompositeController;
import ru.futurelink.mo.web.controller.CompositeParams;
import ru.futurelink.mo.web.exceptions.InitException;

/**
 * Data picker control.
 * 
 * This control is used to select referenced data from the list.
 * 
 * @author Futurelink
 *
 */
public class DataPicker extends CommonField {

	public static String	HINTSTRING = "- не выбрано -";

	protected Text	mEdit;
	private Label	mSelectButton;
	private Label	mClearButton;
	
	private ModifyListener 			mModifyListener;
	private PrepareListener			mPrepareListener;
	
	private Class<? extends ICommonObject> 		mDataClass;
	private Class<?>								mTableClass;
	private Class<? extends CommonItemController> mItemControllerClass;
	private CompositeParams						mItemDialogParams;
	
	private Class<? extends DataPickerController> mPickerController;
	
	private String		mDisplayFieldName;
	private String		mDisplayFieldGetterName;
	
	private String		mSelectedFieldName;
	private String		mSelectedFieldGetterName;

	private Map<String, ArrayList<Object>> mQueryConditions;
	private String		mOrderBy;	
	
	private boolean		allowCreate;	// Allow creation from selection list
	private boolean		publicData;		// Don't care about owner-creator of data 
	
	protected CommonItemControllerListener mParentControllerListener;
	
	public DataPicker(ApplicationSession session, 
			CommonComposite parent, 
			int style, 
			CompositeParams params, 
			CommonItemComposite dataComposite,
			Class<? extends DataPickerController> pickerController
			) {
		super(session, parent, style, params, dataComposite);
		
		mPickerController = pickerController;		
		createControls(style);
	}

	public DataPicker(ApplicationSession session, 
			CommonComposite parent, 
			int style, 
			CompositeParams params,
			CommonDTO dto,
			Class<? extends DataPickerController> pickerController
			) {
		super(session, parent, style, params, dto);

		mPickerController = pickerController;
		createControls(style);
	}
	
	protected void createControls(int style) {
		control = new CommonComposite(getSession(), parent, SWT.BORDER, null);

		GridLayout gl = new GridLayout(3, false);
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		gl.horizontalSpacing = 0;
		gl.verticalSpacing = 0;

		((CommonComposite)control).setLayout(gl);

		mEdit = new Text((Composite) control, SWT.NONE | SWT.READ_ONLY | style);
		mEdit.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		mEdit.pack();

		GridData gd = new GridData();
		gd.verticalAlignment = GridData.CENTER;
		gd.heightHint = 24;
		gd.widthHint = 24;

		// Create clear button
		mClearButton = new Label((Composite) control, SWT.NONE);
		mClearButton.setLayoutData(gd);
		mClearButton.setData(RWT.CUSTOM_VARIANT, "dataPickerButton");
		mClearButton.setImage(new Image(control.getDisplay(), getClass().getResourceAsStream("/images/24/delete.png")));
		mClearButton.pack();
		if ((style & SWT.READ_ONLY) > 0) mClearButton.setEnabled(true);
		mClearButton.addMouseListener(new MouseListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void mouseUp(MouseEvent arg0) {
				
			}
			
			@Override
			public void mouseDown(MouseEvent arg0) {
				try {
					setSelectedDTO(null);
					refresh();
				} catch (DTOException ex) {
					// TODO handle this error!
				}
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {}
		});
		
		GridData gd2 = new GridData();
		gd2.verticalAlignment = GridData.CENTER;
		gd2.heightHint = 24;
		gd2.widthHint = 24;

		// Create selection button
		mSelectButton = new Label((Composite) control, SWT.NONE);
		mSelectButton.setLayoutData(gd2);
		mSelectButton.setData(RWT.CUSTOM_VARIANT, "dataPickerButton");
		mSelectButton.setImage(new Image(control.getDisplay(), getClass().getResourceAsStream("/images/24/find.png")));
		mSelectButton.pack();
		if ((style & SWT.READ_ONLY) > 0) mSelectButton.setEnabled(true);
		mSelectButton.addMouseListener(new MouseListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void mouseUp(MouseEvent arg0) {
				openSelectionDialog();
			}

			@Override
			public void mouseDown(MouseEvent arg0) {}
			
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {}
		});
		
		mQueryConditions = new HashMap<String, ArrayList<Object>>();		
	}

	/**
	 * Get current selected item's DTO.
	 *  
	 * @return
	 */
	public IDTO getSelectedDTO() throws DTOException {
		if (getDTO() != null)
			return (IDTO) getDTO().getDataField(dataFieldName, dataFieldGetter, dataFieldSetter);
		else
			return null;
	}

	/**
	 * Set current selected item DTO.
	 * 
	 * @param data
	 */
	public void setSelectedDTO(IDTO data) throws DTOException {
		/*if (((data != null) && !data.equals(mSelectedData)) || 
			((data == null) && (mSelectedData != null))) {
			mSelectedData = data;*/

			// Set current selected item with value got from getDTO() method
			if (getDTO() != null) {
				Object dataToSet = null;

				// This is for filter DTO only
				if (FilterDTO.class.isAssignableFrom(getDTO().getClass()) && getUseOnlyOneCondition())
					getDTO().setDataField(dataFieldName, dataFieldGetter, dataFieldSetter, null);

				// If we need to set data from DTO field, get it an set.
				if ((data != null) && (mSelectedFieldName != null) && (mSelectedFieldGetterName != null))
					dataToSet = data.getDataField(mSelectedFieldName, mSelectedFieldGetterName, null);
				else
					dataToSet = data;			
				getDTO().setDataField(dataFieldName, dataFieldGetter, dataFieldSetter, dataToSet);
			}

			if (mModifyListener != null) {
				Event e = new Event();
				e.widget = control;
				e.display = control.getDisplay();
				mModifyListener.modifyText(new ModifyEvent(e)); // Отправляем событие
			}		
		//}
	}

	@Override
	public void refresh() throws DTOException {
		if (!mDisplayFieldName.isEmpty() && !mDisplayFieldGetterName.isEmpty()) {
			if (getSelectedDTO() != null)
				mEdit.setText(
					((CommonDTO)getSelectedDTO()).getDataFieldAsString(mDisplayFieldName, mDisplayFieldGetterName, "")
				);
			else
				//mEdit.setText(getLocaleString("noValue"));
				if (!mEdit.isDisposed() && !mEdit.isFocusControl())
					mEdit.setText(HINTSTRING);
		}

		handleMandatory();
	}
	
	/**
	 * Get data picker selection controller class.
	 * 
	 * @return
	 */
	final public Class<? extends DataPickerController> getPickerController() {
		return mPickerController;
	}
	
	/**
	 * Set active data field to display its value in data picker field.
	 * 
	 * @param fieldName
	 * @param getterName
	 * @param setterName
	 */
	final public void setDisplayField(String fieldName, String getterName) {
		mDisplayFieldName = fieldName;
		mDisplayFieldGetterName = getterName;
	}
	
	/**
	 * Set active data field to take selected value from.
	 * 
	 * @param fieldName
	 * @param getterName
	 */
	final public void setSelectedField(String fieldName, String getterName) {
		mSelectedFieldGetterName = getterName;
		mSelectedFieldName = fieldName;
	}
	
	/**
	 * Set data modify listener.
	 */
	final public void addModifyListener(ModifyListener listener) {
		mModifyListener = listener;
	}

	/**
	 * Remove data modify listener.
	 */
	final public void removeModifyListener() {
		mModifyListener = null;
	}

	/**
	 * Set data selection preparation procedure listener.
	 * 
	 * @param listener
	 */
	final public void addPrepareListener(PrepareListener listener) {
		mPrepareListener = listener;
	}
	
	/**
	 * Remove data selection preparetion procedure listener.
	 */
	final public void removePrepareListener() {
		mPrepareListener = null;
	}
	
	final public PrepareListener getPrepareListener() {
		return mPrepareListener;
	}
	
	/**
	 * Set data item class to use in selection.
	 * 
	 * @param classType
	 */
	final public void setDataClass(Class<? extends ICommonObject> classType) {
		mDataClass = classType;
	}

	/**
	 * Set table implementation class to display.
	 * 
	 * @param classType
	 */
	final public void setTableClass(Class<?> classType) {
		mTableClass = classType;
	}

	/**
	 * Get referenced (picked) data entity class.
	 * 
	 * @return
	 */
	final public Class<? extends ICommonObject> getDataClass() {
		return mDataClass;
	}
	
	/**
	 * Get display table class.
	 * 
	 * @return
	 */
	final public Class<?> getTableClass() {
		return mTableClass;
	}

	public void addQueryCondition(String fieldName, Object value) {
		if (mQueryConditions.containsKey(fieldName)) {
			mQueryConditions.get(fieldName).add(value);
		} else {
			ArrayList<Object> values = new ArrayList<Object>();
			values.add(value);
			mQueryConditions.put(fieldName,  values);
		}
	}

	final public String getOrderBy() {
		return mOrderBy;
	}

	final public void setOrderBy(String fieldName) {
		mOrderBy = fieldName;
	}
	
	public Map<String, ArrayList<Object>> getQueryConditions() {
		return mQueryConditions;
	}

	public void clearQueryConditions() {
		mQueryConditions.clear();
		
	}
	
	@Override
	public void setEditable(boolean isEditable) {
		mSelectButton.setEnabled(isEditable);
		mClearButton.setEnabled(isEditable);
	}

	@Override
	public boolean getEditable() {
		return mSelectButton.getEnabled();
	}

	@Override
	public void setParentControllerListener(CommonItemControllerListener listener) {
		mParentControllerListener = listener;		
	}

	@Override
	public boolean isEmpty() {
		try {
			return (getSelectedDTO() == null);
		} catch (DTOException ex) {
			ex.printStackTrace();				
			return true;
		}
	}
		
	@Override
	public void handleMandatory() {
		Color c;
		if (getMandatory()&&isEmpty()) {
			c = new Color(mEdit.getDisplay(), 255, 169, 169);
		} else {
			c = new Color(mEdit.getDisplay(), 255, 255, 255);
		}
		mEdit.setBackground(c);
		control.setBackground(c);
	}

	/**
	 * Set permission to create referenced data from selection list.
	 * 
	 * @param allowCreate
	 */
	public void setAllowCreate(boolean allowCreate) {
		this.allowCreate = allowCreate;
	}

	/**
	 * Get ability to create referenced data from selection list.
	 * 
	 * @return
	 */
	public boolean getAllowCreate() {
		return allowCreate;
	}

	/**
	 * Is the data picker operates on all data or only on own referenced data.
	 * 
	 * @return
	 */
	public boolean getPublic() {
		return publicData;
	}

	/**
	 * Allow select any items from data picker if it's public,
	 * or only current access user created items for non-public.
	 * 
	 * @param pub
	 */
	public void setPublic(boolean pub) {
		publicData = pub;
	}

	/**
	 * Установить класс контроллера элемента для создания
	 * и просмотра элементов из списка выбора.
	 * 
	 * @param class1
	 */
	public void setItemControllerClass(Class<? extends CommonItemController> clazz) {
		mItemControllerClass = clazz;
	}

	/**
	 * Get selectable data item controller class. The class is used for items creation function which is
	 * enabled by setAllowCreate() method.
	 * 
	 * @return
	 */
	public Class<? extends CommonItemController> getItemControllerClass() {
		return mItemControllerClass;
	}

	/**
	 * Set parameters of item creation dialog widget.
	 * 
	 * @param params
	 */
	public void setItemDialogParams(CompositeParams params) {
		mItemDialogParams = params;
	}

	/**
	 * Get parameters of item creation dialog widget.
	 * 
	 * @return
	 */
	public CompositeParams getItemDialogParams() {
		return mItemDialogParams;
	}
	
	@Override
	public String getDataFieldName() {
		return mDisplayFieldName;
	}

	@Override
	public Object getValue() {
		try {
			return getSelectedDTO();
		} catch (DTOException e) {
			e.printStackTrace();
			
			return null;
		}
	}
	
	/**
	 * Get params passed to data picker on preparing
	 * into CompositeParams collection.
	 * 
	 * @return
	 */
	protected CompositeParams getParams() {
		return (new CompositeParams()).
			// Параметры выборки пикера
			add("tableClass", getTableClass()).
			add("queryConditions", getQueryConditions()).
			add("orderBy", getOrderBy()).

			// Параметры, которые касаются возможностей пикера
			add("itemControllerClass", getItemControllerClass()).
			add("itemDialogParams", getItemDialogParams()).
			add("allowCreate", getAllowCreate()).
			add("public", getPublic());		
	}
	
	/**
	 * Open referenced data selection window.
	 * 
	 * @param picker
	 */
	protected void openSelectionDialog() {
		// Can not open selection until parent controller is set
		if (getParentController() == null) return;
		
		CommonDialog d = new CommonDialog(getSession(), getParentController().getComposite().getShell(), SWT.NONE);		
		Class<? extends DataPickerController> pickerControllerClass = getPickerController();
		if (pickerControllerClass == null) {
			pickerControllerClass = SimpleDataPickerController.class;
		}

		// Вытащим конструктор окна выбора
		Constructor<?> constr;
		try {
			constr = pickerControllerClass.getConstructor(
					CompositeController.class,
					Class.class,
					Composite.class,
					CompositeParams.class);
		} catch (NoSuchMethodException | SecurityException ex1) {
			getParentController().handleError("Ошибка получения конструктора для окна выбора из списка.", ex1);
			return;
		}

		// Prepare data for list selection
		if (getPrepareListener() != null)
			getPrepareListener().prepare();
		
		DataPickerController c = null;
		d.setText(((CommonComposite)getParentController().getComposite()).getLocaleString("selection"));				
		try {		
			c = (DataPickerController) constr.newInstance(
					(CompositeController)getParentController(),
					getDataClass(),
					d.getShell(),
					getParams());
			c.init();
			// If there was an error in composite creation it must do nothing
			if (c.getComposite() != null && !c.getComposite().isDisposed()) {
				d.attachComposite(c.getComposite());
				if (getSession().getMobileMode()) {
					// In mobile mode we use fullscreen list sizing
					d.setSize(CommonDialog.FULL);
				} else {
					// In desktop mode we use screen-relative list sizing
					d.setSize(CommonDialog.LARGE);
				}
				d.open();
			
				// Пикеру просетили элемент DTO чтобы он уже отобразил данные на своем поле
				if ((d.getResult() != null) && (d.getResult().equals("save"))) {
					try {
						setSelectedDTO(c.getActiveData());
						refresh();
					} catch (DTOException ex) {
						// TODO handle this error!
					}
				}
			}
		} catch (IllegalArgumentException ex) {
			getParentController().handleError("Ошибка создания диалога выбора из справочника.", ex);
		} catch (InvocationTargetException | IllegalAccessException | InstantiationException | InitException ex) {
			getParentController().handleError("Ошибка создания диалога выбора из справочника.", ex);
		} finally {
			constr = null;
			c = null;
			d = null;			
		}
	}
	
}
