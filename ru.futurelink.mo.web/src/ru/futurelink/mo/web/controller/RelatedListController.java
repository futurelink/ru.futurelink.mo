package ru.futurelink.mo.web.controller;

import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.EditorDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.exceptions.SaveException;
import ru.futurelink.mo.web.controller.iface.ICompositeController;
import ru.futurelink.mo.web.controller.iface.IListEditController;
import ru.futurelink.mo.web.exceptions.InitException;

/**
 * Контроллер связанных данных. Используется для добавления к юзкейсам элементов
 * данных списков связанных по определенному полю. К примеру, есть документ, а у него
 * есть список товаров. В представлении элемента документа таблица товаров - это набор
 * данных хранимых в отдельной таблице, представленный отдельным классом и связанный
 * по определенному полю.
 * 
 * Отображением данных для этого контроллера может быть любая таблица или кастомный отображатель,
 * например календарь.
 * 
 * Список объектов поднимается на память из базы при открытии элемента и держится до момента закрытия
 * главного элемента данных, к которому он привязан. Все модификации данных осуществляются НА ПАМЯТИ 
 * процесса.
 * 
 * @author pavlov
 *
 */
public abstract class RelatedListController 
	extends CommonListController
	implements RelatedController, IListEditController{

	private CommonItemController		mRelatedController;
	private String						mRelatedFieldName;	
	private String						mRelationFieldName;

	private boolean					mDataChanged;

	public RelatedListController(ICompositeController parentController,
			Class<? extends CommonObject> dataClass, Composite container,
			CompositeParams compositeParams) {
		super(parentController, dataClass, container, compositeParams);
		
		setRelatedController((CommonItemController) parentController);
	}

	public RelatedListController(ICompositeController parentController,
			Class<? extends CommonObject> dataClass, CompositeParams compositeParams) {
		super(parentController, dataClass, compositeParams);
		
		setRelatedController((CommonItemController) parentController);
	}

	/**
	 * Установить контроллер, к которому привязан этот контроллер.
	 * 
	 * @param ctrl объект основного контроллера
	 */
	public final void setRelatedController(CommonItemController ctrl) {
		mRelatedController = ctrl;			// Делаем ссылку на контроллер связи,		
		ctrl.addRelatedController(this);	// и даем ему ссылку на себя.
	}
	
	/**
	 * Получить оьъект контроллера, с которым связан этот контроллер списка.
	 * 
	 * @return объект основного контроллера
	 */
	public final CommonItemController getRelatedController() {
		return mRelatedController;
	}
	
	/**
	 * Получить элемент DTO основного контроллера. Метод сделан для удобства.
	 * 
	 * @return объект DTO от основного контроллера
	 */
	protected final CommonDTO getRelatedDTO() {
		return mRelatedController.getDTO();
	}
	
	/**
	 * 
	 * @param relatedField поле внешней связи (ID)
	 * @param relationField поле внутри связи (someField)
	 */
	public final void setRelation(String relatedField, String relationField) {
		mRelatedFieldName = relatedField;
		mRelationFieldName = relationField;
	}
	
	public final String getRelatedField() {
		return mRelatedFieldName;
	}

	public final String getRelationField() {
		return mRelationFieldName;
	}

	/**
	 * Метод сохранения списка. Сформированный список DTO будет сохранен в базу данных.
	 * Операция должна выполняться в рамках одной транзакции, атомарно. В этом методе не
	 * происходит проверки на наличие открытой транзакции, не производится проверка на
	 * исключения, которые могут приводить к некорректному сохранению. Исключения нужно
	 * обраьотать выше, там, где метод будет вызываться.
	 * 
	 * @throws DTOException
	 * @throws SaveException
	 */
	@Override
	public final void save() throws DTOException, SaveException {
		logger().debug("RelatedListController сохраняет данные...");

		if (getDTO() == null) {
			throw new DTOException("Нет объекта DTO на RelatedListController", null);
		}

		// Сохраняем все элементы из списка DTO композита
		for (CommonDTO dto : getDTO().getDTOList().values()) {
			// Для исторических элементов надо пересвязать данные
			logger().debug("Данные будут перепривязаны к главному элменту с ID=" + 
					getRelatedDTO().getId());
			
			doBeforeItemSave(dto);
			((EditorDTO)dto).save();
			doAfterItemSave(dto);
			
			((EditorDTO)dto).forceUpdateField(getRelationField(), (CommonDTO) getRelatedDTO());
		}
		logger().debug("RelatedListController данные cохранены.");
	}
	
	/**
	 * Выполнять перед сохранением элемента в момент сохранения всего списка. 
	 * 
	 * @param item сохраняемый объект DTO
	 */
	public void doBeforeItemSave(CommonDTO item) throws DTOException, SaveException {}
	
	/**
	 * Выполнять после сохранения элемент в момент сохранения всего списка.
	 * 
	 * @param item сохраняемый объект DTO
	 */
	public void doAfterItemSave(CommonDTO item) throws DTOException, SaveException {}
	
	@Override
	public void refresh(boolean refreshSubcontrollers) throws DTOException {
		handleDataQuery();
	}

	/**
	 * Получить состояние, изменились ли данные в списке?
	 * 
	 * @return
	 */
	@Override
	public boolean getChanged() {
		return mDataChanged;
	}
	
	@Override
	public void handleCreate() throws DTOException, InitException {
		mDataChanged = true;

		// Отправялем сигнал об изменении данных родительскому контроллеру через его композит
		((CommonItemControllerListener)getRelatedController().getComposite().getControllerListener()).dataChanged(null);
	}
	
	@Override
	public void handleDelete() throws DTOException {
		mDataChanged = true;

		// Отправялем сигнал об изменении данных родительскому контроллеру через его композит
		((CommonItemControllerListener)getRelatedController().getComposite().getControllerListener()).dataChanged(null);
	}
	
	@Override
	public void handleEdit() throws DTOException {
		mDataChanged = true;

		// Отправялем сигнал об изменении данных родительскому контроллеру через его композит
		((CommonItemControllerListener)getRelatedController().getComposite().getControllerListener()).dataChanged(null);		
	}
	
	@Override
	public SaveMode getSaveMode() {
		return SaveMode.SAVE_AFTER;
	}
}
