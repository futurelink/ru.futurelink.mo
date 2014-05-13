package ru.futurelink.mo.web.composites.fields;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Control;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.CommonItemComposite;
import ru.futurelink.mo.web.controller.CommonControllerListener;
import ru.futurelink.mo.web.controller.CommonItemControllerListener;
import ru.futurelink.mo.web.controller.CompositeParams;

abstract public class CommonField implements IField {
	protected Control 		mControl;
	protected String		mDataFieldName;
	protected String		mDataFieldSetter;
	protected String		mDataFieldGetter;
	protected CommonItemControllerListener mParentControllerListener;
	private	   CommonItemComposite		mDataComposite;
	private	   CommonDTO				mDTO;
	protected ModifyListener			mFieldModifyListener;
	private	   boolean					mMandatoryFlag;
	private	   boolean					mUseOnlyOneCondition = true;
	protected CommonComposite			mParent;

	public CommonField(ApplicationSession session, CommonComposite parent, int style,
			CompositeParams params, CommonItemComposite dataComposite) {		
		mParent = parent;
		mDataComposite = dataComposite;
	}

	public CommonField(ApplicationSession session, CommonComposite parent, int style,
			CompositeParams params, CommonDTO dto) {
		mParent = parent;		
		mDTO = dto;
	}

	public void addModifyListener(ModifyListener listener) {
		mFieldModifyListener = listener;
	}

	public void setDataField(String dataField, String dataFieldGetter, String dataFieldSetter) {		
		mDataFieldName = dataField;
		mDataFieldGetter = dataFieldGetter;
		mDataFieldSetter = dataFieldSetter;
	}

	/**
	 * Метод возвращает обработчик, привязанный к композиту элемента
	 * данных (CommonItemComposite).
	 */
	@Override
	public CommonControllerListener getControllerListener() {
		if (mDataComposite != null)
			return mDataComposite.getControllerListener();
		return null;
	}
	
	/**
	 * Установить полю ссылку на обработчик роительского контроллера.
	 */
	public void setParentControllerListener(CommonItemControllerListener listener) {
		mParentControllerListener = listener;
	}

	/**
	 * Обработать обязательность поля. Показать как-либо маркер, свойственный данному полю.
	 */
	abstract protected void handleMandatory();
	
	/**
	 * Получить элемент DTO для поля:
	 * - если используется в режиме с DataComposite - возвращается элемент DTO из окна отображения,
	 * - если используется с конструктором в который передан DTO - возвращается сохраненный DTO.
	 * @return
	 */
	@Override
	public CommonDTO getDTO() {
		if ((mDataComposite == null) || (mDTO != null)) return mDTO;
		if (mDataComposite.getDTO() != null) {
			return mDataComposite.getDTO();
		} else {
			return null;
		}
	}
	
	/**
	 * Установить объект DTO для работы с полем.
	 * 
	 * @param dto
	 */
	@Override
	public void setDTO(CommonDTO dto) {
		mDTO = dto;
	}
	
	/**
	 * Получить объект самого себя. Используется в реализациях некоторых обработчиков.
	 * 
	 * @return
	 */
	public final CommonField getSelf() {
		return this;
	}
	
	/**
	 * Очистить поле до состояния по-умолчанию.
	 */
	@Override
	public void clear() {}

	/**
	 * Установить признак обязательности поля.
	 *
	 * @param isMandatory
	 * @throws DTOException 
	 */
	@Override
	public final void setMandatory(boolean isMandatory) {
		mMandatoryFlag = isMandatory;
	}
	
	/**
	 * Получить признак обязательности поля.
	 * 
	 * @return
	 */
	@Override
	public final boolean getMandatory() {
		return mMandatoryFlag;
	}

	@Override
	public void setEditable(boolean isEditable) {
		mControl.setEnabled(isEditable);
	}

	@Override
	public boolean getEditable() {
		return mControl.getEnabled();
	}
	
	@Override
	public String getDataFieldName() {
		return mDataFieldName;
	}

	@Override
	public String getDataFieldGetter() {
		return mDataFieldGetter;
	}

	@Override
	public String getDataFieldSetter() {
		return mDataFieldSetter;
	}
		
	public boolean getUseOnlyOneCondition() {
		return mUseOnlyOneCondition;
	}
	
	public void setUseOnlyOneCondition(boolean useOnlyOneCondition) {
		mUseOnlyOneCondition = useOnlyOneCondition;
	}

	public void setFont(Font font) {
		mControl.setFont(font);
	}
	
	public Control getControl() {
		return mControl;
	}
	
	public void setFocus() {
		mControl.setFocus();
	}

	public void setEnabled(boolean enabled) {
		mControl.setEnabled(enabled);
	}
	
	public void setLayoutData(Object layoutData) {
		mControl.setLayoutData(layoutData);
	}
}
