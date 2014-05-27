package ru.futurelink.mo.web.composites.fields.datapicker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.FilterDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.CommonItemComposite;
import ru.futurelink.mo.web.composites.fields.CommonField;
import ru.futurelink.mo.web.composites.fields.IField;
import ru.futurelink.mo.web.controller.CommonItemController;
import ru.futurelink.mo.web.controller.CommonItemControllerListener;
import ru.futurelink.mo.web.controller.CompositeParams;

/**
 * Простейший контрол выбора элемента из списка.
 * 
 * @author Futurelink
 *
 */
public class DataPicker extends CommonField implements IField {

	private Text	mEdit;
	private Label	mSelectButton;
	private Label  mClearButton;
	
	private ModifyListener 			mModifyListener;
	private SelectionListener		mDataPickListener;
	
	private Class<? extends CommonObject> 			mDataClass;
	private Class<?>								mTableClass;
	private Class<? extends CommonItemController> mItemControllerClass;
	private CompositeParams							mItemDialogParams;
	
	private Class<? extends CommonDataPickerController> mPickerController;
	
	private String			mDisplayFieldName;
	private String			mDisplayFieldGetterName;
	
	private Map<String, ArrayList<Object>> mQueryConditions;
	private String			mOrderBy;	
	
	// Свойства используемые для определения 
	// создания элемента из списка выбора
	private boolean		mAllowCreate;
	
	protected CommonItemControllerListener mParentControllerListener;
	
	public DataPicker(ApplicationSession session, 
			CommonComposite parent, 
			int style, 
			CompositeParams params,
			CommonDTO dto,
			Class<? extends CommonDataPickerController> pickerController
			) {
		super(session, parent, style, params, dto);
		
		mPickerController = pickerController;

		createControls(style);
	}
	
	public DataPicker(ApplicationSession session, 
			CommonComposite parent, 
			int style, 
			CompositeParams params, 
			CommonItemComposite dataComposite,
			Class<? extends CommonDataPickerController> pickerController) {
		super(session, parent, style, params, dataComposite);
		
		mPickerController = pickerController;
		
		createControls(style);
	}

	private void createControls(int style) {
		mControl = new CommonComposite(mParent.getSession(), mParent, SWT.READ_ONLY, null);

		GridLayout gl = new GridLayout(3, false);
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		gl.horizontalSpacing = 0;
		gl.verticalSpacing = 0;

		((CommonComposite)mControl).setLayout(gl);

		mEdit = new Text((Composite) mControl, SWT.BORDER | SWT.READ_ONLY);
		mEdit.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		mEdit.pack();

		mClearButton = new Label((Composite) mControl, SWT.NONE);
		mClearButton.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		mClearButton.setData(RWT.CUSTOM_VARIANT, "dataPickerButton");
		mClearButton.setImage(new Image(mControl.getDisplay(), getClass().getResourceAsStream("/images/24/delete.png")));
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
					// TODO hand;e this error!
				}
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {}
		});
		
		mSelectButton = new Label((Composite) mControl, SWT.NONE);
		mSelectButton.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		mSelectButton.setData(RWT.CUSTOM_VARIANT, "dataPickerButton");
		mSelectButton.setImage(new Image(mControl.getDisplay(), getClass().getResourceAsStream("/images/24/find.png")));
		mSelectButton.pack();
		if ((style & SWT.READ_ONLY) > 0) mSelectButton.setEnabled(true);
		mSelectButton.addMouseListener(new MouseListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void mouseUp(MouseEvent arg0) {}
			
			@Override
			public void mouseDown(MouseEvent arg0) {
				if (mDataPickListener != null) {			
					Event e = new Event();
					e.data = arg0.data;
					e.stateMask = arg0.stateMask;
					e.widget = arg0.widget;
					e.display = arg0.display;
					e.x = arg0.x;
					e.y = arg0.y;
					SelectionEvent se = new SelectionEvent(e);
					mDataPickListener.widgetSelected(se);
				}
			}
			
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
	public CommonDTO getSelectedDTO() throws DTOException {
		if (getDTO() != null)
			return (CommonDTO) getDTO().getDataField(mDataFieldName, mDataFieldGetter, mDataFieldSetter);
		else
			return null;
	}

	/**
	 * Set current selected item DTO.
	 * 
	 * @param data
	 */
	public void setSelectedDTO(CommonDTO data) throws DTOException {
		// Только если реально данные изменились на уровне объекта
		/*if (((data != null) && !data.equals(mSelectedData)) || 
			((data == null) && (mSelectedData != null))) {
			mSelectedData = data;*/

			// Set current selected item with value got from getDTO() method
			if (getDTO() != null) {
				// Это касается фильтра
				if (FilterDTO.class.isAssignableFrom(getDTO().getClass()) && getUseOnlyOneCondition())
					getDTO().setDataField(mDataFieldName, mDataFieldGetter, mDataFieldSetter, null);

				getDTO().setDataField(mDataFieldName, mDataFieldGetter, mDataFieldSetter, data);
			}

			if (mModifyListener != null) {
				Event e = new Event();
				e.widget = mControl;
				e.display = mControl.getDisplay();
				mModifyListener.modifyText(new ModifyEvent(e)); // Отправляем событие
			}
		//}			
	}
	
	@Override
	public void refresh() throws DTOException {
		// Display selected item display field value
		if (!mDisplayFieldName.isEmpty() && !mDisplayFieldGetterName.isEmpty()) {
			if (getSelectedDTO() != null)
				mEdit.setText(
					getSelectedDTO().getDataFieldAsString(mDisplayFieldName, mDisplayFieldGetterName, "")
				);
			else
				//mEdit.setText(getLocaleString("noValue"));
				mEdit.setText("- не выбрано -");
		}

		handleMandatory();
	}
	
	/**
	 * Получить класс контроллера списка выбора для этого элмента.
	 * 
	 * @return
	 */
	final public Class<? extends CommonDataPickerController> getPickerController() {
		return mPickerController;
	}
	
	/**
	 * Задать поле DTO отображения данных в поле выбора.
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
	 * Привязать обработчик изменения выбора.
	 */
	final public void addModifyListener(ModifyListener listener) {
		mModifyListener = listener;
	}

	/**
	 * Отвязать обработчик изменения выбора.
	 */
	final public void removeModifyListener() {
		mModifyListener = null;
	}

	/**
	 * Привязать обработчик кнопки выбора данных.
	 * 
	 * @param listener
	 */
	final public void setDataPickListener(SelectionListener listener) {
		mDataPickListener = listener;
	}
	
	/**
	 * Отвязать обработчик кнопки выбора данных.
	 */
	final public void removeDataPickListener() {
		mDataPickListener = null;
	}
	
	/**
	 * Задать класс данных для списка выбора.
	 * 
	 * @param classType
	 */
	final public void setDataClass(Class<? extends CommonObject> classType) {
		mDataClass = classType;
	}

	/**
	 * Задать класс таблицы для отображения списка.
	 * 
	 * @param classType
	 */
	final public void setTableClass(Class<?> classType) {
		mTableClass = classType;
	}

	final public Class<? extends CommonObject> getDataClass() {
		return mDataClass;
	}
	
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
		if (getMandatory()&&isEmpty()) {
			mEdit.setBackground(new Color(mEdit.getDisplay(), 255, 169, 169));
		} else {
			mEdit.setBackground(new Color(mEdit.getDisplay(), 255, 255, 255));
		}
	}

	/**
	 * Можно создавать элементы из списка выбора?
	 * 
	 * @param allowCreate
	 */
	public void setAllowCreate(boolean allowCreate) {
		mAllowCreate = allowCreate;
	}

	/**
	 * Получить можно ли создавать элементы из списка выбора.
	 * 
	 * @return
	 */
	public boolean getAllowCreate() {
		return mAllowCreate;
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
	
}
