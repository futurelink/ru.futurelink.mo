package ru.futurelink.mo.web.composites;

import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.CommonDTOList;
import ru.futurelink.mo.orm.dto.EditorDTOList;
import ru.futurelink.mo.orm.dto.FilterDTO;
import ru.futurelink.mo.orm.dto.access.AllowOwnChecker;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.controller.CompositeParams;
import ru.futurelink.mo.web.controller.iface.IListComposite;
import ru.futurelink.mo.web.controller.iface.ListDTOAccessor;

/**
 * Класс композита для работы со списочными данными, реализует все, что унаследовано от
 * родительского класса CommonDataComposite.
 * 
 * Объект этого класса имеет внутри ссылку на коллекцию CommonDTO объектов, которые используются
 * в нем для доступа к данным.
 * 
 * @author pavlov
 * @param <T>
 *
 */
abstract public class CommonListComposite 
	extends CommonDataComposite
	implements IListComposite {

	private static final long serialVersionUID = 1L;
	
	protected CommonDTOList<? extends CommonDTO>	mDTO;
	private CommonDTO								mActiveData;
	
	public CommonListComposite(ApplicationSession session,
			Composite parent, int style, CompositeParams params) {
		super(session, parent, style, params);
		mDTO = new EditorDTOList<CommonDTO>(getSession().persistent(), new AllowOwnChecker(getSession().getUser()), CommonDTO.class);
	}

	/**
	 * Приаттачить список CommonDTO объектов для работы в списочном композите.
	 * Лучше не вызывать из
	 * 
	 * @param data коллекция CommonDTO
	 * @throws DTOException 
	 */
	protected void attachDTO(CommonDTOList<? extends CommonDTO> data) throws DTOException {
		mDTO = data;				
		refresh();
	}

	/**
	 * Удалить и отвязать модель (список CommonDTO) от списка.
	 * @throws DTOException 
	 */
	protected void removeDTO() throws DTOException {
		// Удаляем все DTO ссылки из элемента, а перед этим чистим DTO
		if (mDTO != null) {		
			mDTO = null;			
			refresh();
		}
	}
	
	/**
	 * Получить приаттаченую коллекцию объектов CommonDTO, либо коллекцию объектов
	 * DTO от контроллера через обработчик. При этом обработчик должен реализовывать
	 * интерфейс ListDTOAccessor. Если обработчика нет, то будет возвращаться внутренняя
	 * коллекция объектов.
	 * 
	 * @return - коллекция CommonDTO
	 */
	@Override
	public CommonDTOList<? extends CommonDTO> getDTO() throws DTOException {
		if (getControllerListener() != null) {
			
			// Если у нас обработчик контроллера не кастуется в обработчик контроллера списка,
			// то надо обработать это и вывалить эксепшн.
			if (!(ListDTOAccessor.class.isAssignableFrom(getControllerListener().getClass()))) {
				throw new DTOException("Неправильный обработчик списка на контроллере списка. "
						+ "Обработчик контроллера должен унаследовать "
						+ "ListDTOAccessor и метод getControllerDTO()", null);
			}

			if (((ListDTOAccessor)getControllerListener()).getControllerDTO() != null) {
				return ((ListDTOAccessor)getControllerListener()).getControllerDTO();
			}
		}

		return mDTO;
	}

	/**
	 * Получить объект фильтра.
	 * 
	 * @return объект FilterDTO
	 * @throws DTOException
	 */
	@Override
	public FilterDTO getFilter() throws DTOException {
		if (getParam("filter") != null)
			return (FilterDTO)getParam("filter");
		else
			throw new DTOException("Нет объекта фильтра переданного из контроллера!", null);
	}

	/**
	 * Метод сохраняет в объекте указатель на выбранный элемент из коллекции списочных данных.
	 * 
	 * @param data элемент CommonDTO, желательно, чтобы он присутствовал данных модели
	 */
	@Override
	public void setActiveData(CommonDTO data) {
		mActiveData = data;
	}

	/**
	 * Получить выбранный элемент из коллекции списочных данных.
	 * 
	 * @return выбранный элемент из модели (коллекции CommonDTO), привязанной к списку
	 */
	@Override
	public CommonDTO getActiveData() {
		return mActiveData;
	}

}
