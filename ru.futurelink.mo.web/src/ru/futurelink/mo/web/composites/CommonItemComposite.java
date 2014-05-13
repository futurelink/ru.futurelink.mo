package ru.futurelink.mo.web.composites;

import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.fields.IField;
import ru.futurelink.mo.web.composites.toolbar.CommonToolbar;
import ru.futurelink.mo.web.controller.CompositeParams;

/**
 * Класс комопзита, имеюзий доступ к одному элементу через EditorDTO. Реализует все,
 * что доступно в родительском классе CommonDataComposite.
 * 
 * @author pavlov
 *
 */
public class CommonItemComposite extends CommonDataComposite {

	private static final long serialVersionUID = 1L;

	private CommonDTO		mDTO;
	private IField[]		mMandatoryFields;
	
	public CommonItemComposite(ApplicationSession session, Composite parent, int style, CompositeParams params) {
		super(session, parent, style, params);
	}

	public void refresh() throws DTOException {}
	
	/**
	 * Привязать объект EditorDTO к композиту.
	 * 
	 * @param data
	 */
	public void attachDTO(CommonDTO data, Boolean refresh) throws DTOException {
		mDTO = data;
		if ((mDTO != null) && (refresh == true))
			refresh();
	}

	/**
	 * Отвязать и удалить объект DTO от композита.
	 */
	public void removeDTO() {
		if (mDTO != null)
			mDTO.clear();
		mDTO = null;
	}

	/**
	 * Чтобы получить для контроллера элемент данных правильного типа,
	 * и не приводить его везде где можно, мы переопределяем
	 * этот геттер.
	 */
	public CommonDTO getDTO() {
		return mDTO;
	}

	/**
	 * Сделать доступной кнопку сохранения. Метод необходимо переопределить,
	 * для конкретной реализации композита с кнопкой "Сохранить".
	 * 
	 * @param enabled
	 */
	public void setSaveEnabled(boolean enabled) {}
	
	/**
	 * Сделать доступной кнопку отката изменений. Метод необходимо переопределить
	 * для конкретной реализации композита с кнопкой "Очистить".
	 * 
	 * @param enabled
	 */
	public void setRevertEnabled(boolean enabled) {}

	@Override
	protected void createContents() {
		createCaptionLabel();
		mWorkspace = createWorkspace();		
		mToolbar = createToolbar();
	}

	@Override
	protected CommonComposite createWorkspace() {
		return null;
	}

	@Override
	protected CommonToolbar createToolbar() {
		return null;
	}

	/**
	 * Проверить, заполнены ли обязательные поля?
	 * 
	 * @return
	 */
	public boolean getIsMandatoryFilled() {
		if (mMandatoryFields != null)
			for (IField field : mMandatoryFields) {
				if (field.isEmpty()) return false;
			}
		return true;
	}
	
	/**
	 * Установить список обязательных полей для формы.
	 * Список устанавливается один раз при создании формы,
	 * далее поле не может быть объявлено как необязательное. 
	 * Пока так.
	 * 
	 * @param fields
	 */
	protected void setMandatoryFields(IField[] fields) {
		// Если у нас есть какие-то поля, обявленные обязательными, надо
		// их сделать необязательными, потом очистить список и дать новый список.
		if (mMandatoryFields != null) {			
			for (IField field : mMandatoryFields) {
				field.setMandatory(false);
			}			
		}

		mMandatoryFields = fields;
		for (IField field : fields) {
			field.setMandatory(true);
		}
	}
}
