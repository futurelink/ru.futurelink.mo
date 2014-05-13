package ru.futurelink.mo.web.controller;

import java.lang.reflect.Constructor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.EditorDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.orm.exceptions.OpenException;
import ru.futurelink.mo.orm.exceptions.SaveException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.composites.SimpleListComposite;
import ru.futurelink.mo.web.composites.CommonListComposite;
import ru.futurelink.mo.web.composites.dialogs.CommonItemDialog;
import ru.futurelink.mo.web.composites.table.CommonTable;
import ru.futurelink.mo.web.controller.iface.IListEditController;
import ru.futurelink.mo.web.exceptions.InitException;

/**
 * Простой контроллер для композита SimpleListComposite. Он по-умолчанию использует
 * SimpleListComposite для отображения данных и его поведение определяется именно
 * этим композитом.
 * 
 * Нужно переопределить его, переопределить handleDataQuery для получения данных,
 * в методе doBeforeCreateComposite:
 * 
 * 		params().add("tableClass", ...); // Класс таблицы для отображения
 *		params().add("itemControllerClass", ...); // Класс контроллера для отображения элемента
 *		params().add("itemDialogTitle", ...); // Заголовок окна редактирования элемента
 * 
 * @author pavlov
 * @since 1.2
 *
 */
abstract public class SimpleListController 
	extends CommonListController
	implements IListEditController {

	public SimpleListController(CompositeController parentController,
			Class<? extends CommonObject> dataClass, CompositeParams compositeParams) {
		super(parentController, dataClass, compositeParams);
	}

	public SimpleListController(CompositeController parentController,
			Class<? extends CommonObject> dataClass, Composite container, CompositeParams compositeParams) { 
		super(parentController, dataClass, container, compositeParams);
	}
	
	public void handleItemDoubleClicked(CommonDTO data) {
		if ((data != null) && (openEditDialog((EditorDTO)data) != null)) {
			// Обновить список, перечитать данные
			try {
				handleDataQuery();
			} catch (DTOException ex) {
				handleError("невозможно обновить список.", ex);
			}
		}
	}
	
	public void addDragSupport(int operations, Transfer[] transferTypes, DragSourceListener listener) {
		CommonTable tbl = ((SimpleListComposite)getComposite()).getTable();
		if (tbl != null)
			tbl.addDragSupport(operations, transferTypes, listener);
	}
	
	// Обработка создания новго элемента
	@Override
	public void handleCreate() {
		if (openEditDialog(null) != null) {	
			// Обновить список, перечитать данные
			try {
				handleDataQuery();
			} catch (DTOException ex) {
				handleError("невозможно обновить список.", ex);
			}
		}
	}
	
	// Обработка удаления элемента
	@Override
	public void handleDelete() {
		String id = "";
		// Только если есть выбранный элемент, есть активные данные
		if (((CommonListComposite)getComposite()).getActiveData() != null) {
			try {
				id = ((CommonListComposite)getComposite()).
						getActiveData().
						getDataField("mId", "getId", "setId").toString();
				CommonObject obj = getSession().persistent().open(getDataClass(), id);
				obj.delete();
				obj.save();
				logger().debug("Удален элемент ID: ", id);
			} catch (DTOException ex) {
				handleError("Ошибка доступа DTO при удалении элемента.", ex);
			} catch (OpenException ex) {
				handleError("Ошибка открытия при удалении элемента.", ex);
			} catch (SaveException ex) {
				handleError("Ошибка сохранения при удалении элемента.", ex);
			}

			// Обновить список, перечитать данные
			try {
				handleDataQuery();
			} catch (DTOException ex) {
				handleError("невозможно обновить список.", ex);
			}			
		}
	}

	// Обработка редактирования элемента
	@Override
	public void handleEdit() {
		handleItemDoubleClicked(((CommonListComposite)getComposite()).getActiveData());
	}

	@Override
	protected CommonComposite createComposite(CompositeParams compositeParams) {
		// Добавлем в параметры объект FilterDTO
		params().add("filter", getFilter());
		
		@SuppressWarnings("unchecked")
		Class<? extends CommonListComposite> listCompositeClass = 
				(Class<? extends CommonListComposite>)params().get("listComposite");
		if (listCompositeClass != null) {
			SimpleListComposite composite = null;
			try {
				Constructor<?> constr = listCompositeClass.getConstructor(ApplicationSession.class,
						Composite.class, int.class, CompositeParams.class);
				composite = (SimpleListComposite) constr.newInstance(
						getSession(), getContainer(), SWT.NONE, params());
			} catch (Exception ex) {
				logger().error("Не удалось создать окно композита для контроллера", ex);
			}
			return composite;
		} else {
			return new SimpleListComposite(getSession(), getContainer(), SWT.NONE, params());
		}
	}

	@Override
	public void handleRecover() throws DTOException {
		
	}

	@Override
	protected void doAfterCreateComposite() {
		// Зацепляем обработчик контроллера на композит
		getComposite().addControllerListener(createControllerListener());
		
		// Запускаем создание колонок таблицы на композите
		((SimpleListComposite)getComposite()).createTableColumns();
		
		try {
			handleDataQuery();
		} catch (DTOException ex) {
			handleError("невозможно обновить список. "+ex.getMessage(), ex);
		}
	}
	
	/**
	 * Открывает окно редактирования выбранного элмента данных.
	 * 
	 * @param data
	 * @return
	 */
	private Object openEditDialog(EditorDTO data) {
		return new CommonItemDialog(getSession(), getComposite(), this, params()).open(data);
	}
	
	@Override
	public CommonControllerListener createControllerListener() {
		return new SimpleListControllerListener(this);
	}
	
	@Override
	protected void doBeforeInit() throws InitException {
		
	}
	
	@Override
	protected void doAfterInit() throws InitException {

	}
	
	@Override
	public void handleDataQueryExecuted() throws DTOException {
		((CommonListComposite)getComposite()).disableToolbar();

		setToolEnabled("create", true);

		((CommonListComposite)getComposite()).refresh();		
	}
	
	/**
	 * Метод обработки изменения размера колонки таблицы.
	 * 
	 * @param column
	 */
	protected void onTableColumnResized(TableColumn column) {}
	
	/**
	 * Метод обрабаотки создания колонки в таблице.
	 * 
	 * @param column
	 */
	protected void onTableColumnAdded(TableColumn column, String columnField, String columnFieldGetter, String columnFieldSetter, Class<?> columnFieldType) {}
}
