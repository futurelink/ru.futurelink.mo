package ru.futurelink.mo.web.composites;

import java.lang.reflect.Constructor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.table.CommonTable;
import ru.futurelink.mo.web.composites.table.CommonTableListener;
import ru.futurelink.mo.web.composites.toolbar.CommonToolbar;
import ru.futurelink.mo.web.composites.toolbar.JournalToolbar;
import ru.futurelink.mo.web.composites.toolbar.ToolbarListener;
import ru.futurelink.mo.web.controller.CommonTableControllerListener;
import ru.futurelink.mo.web.controller.CompositeParams;
import ru.futurelink.mo.web.controller.CommonListControllerListener;
import ru.futurelink.mo.web.exceptions.CreationException;
import ru.futurelink.mo.web.exceptions.InitException;

/**
 * <p>Упрощенный композит списка. Позволяет реализовать стандартный список путем
 * передачи параметров и простого конфигурирования. Позволяет создавать, править,
 * удалять элементы кнопками на тулбаре. По двойному клику в таблице - операция 
 * редактирования.</p>
 * 
 * <p>В compositeParams передается "tableClass" - класс реализации CommonTable.</p>
 *  
 * @author pavlov
 * @since 1.2
 *
 */
public class SimpleListComposite extends CommonListComposite {

	private static final long serialVersionUID = 1L;

	protected	CommonTable	mTable;
	private		Class<?>	mTableClass;
	
	public SimpleListComposite(ApplicationSession session, Composite parent, int style, CompositeParams params) {
		super(session, parent, style, params);
	}

	@Override
	public void refresh() throws DTOException {
		if (mTable != null && (getDTO() != null))
			mTable.setInput(getDTO());
	}

	@Override
	protected CommonComposite createWorkspace() throws CreationException {
		mTableClass = (Class<?>) getParam("tableClass");
		if (mTableClass == null) {
			throw new CreationException("SimpleListComposite: не указан параметр tableClass в CompositeParams.");
		}

		try {
			Constructor<?> constr = mTableClass.getConstructor(
					ApplicationSession.class, 
					Composite.class, 
					int.class, 
					CompositeParams.class);
			mTable = (CommonTable) constr.newInstance(getSession(), this, SWT.NONE, null);
			mTable.addTableListener(new CommonTableListener() {			
				@Override
				public void itemSelected(CommonDTO data) {
					setActiveData(data);
					if (getControllerListener() != null)
						((CommonListControllerListener)getControllerListener()).itemSelected(data);
				}
				
				@Override
				public void itemDoubleClicked(CommonDTO data) {
					if (getControllerListener() != null)
						((CommonListControllerListener)getControllerListener()).itemDoubleClicked(data);
				}

				@Override
				public void onColumnResized(TableColumn column) {
					if (getControllerListener() != null)
						((CommonTableControllerListener)getControllerListener()).onColumnResized(column);
				}

				@Override
				public void onColumnAdded(TableColumn column,
						String filterField, 
						String filterFieldGetter,
						String filterFieldSetter,
						Class<?> filterFieldType) {
					if (getControllerListener() != null)						
						((CommonTableControllerListener)getControllerListener()).onColumnAdded(
								column, 
								filterField, 
								filterFieldGetter, 
								filterFieldSetter,
								filterFieldType);					
				}
			});
		} catch (Exception ex) {
			getControllerListener().sendError("SimpleListComposite: Ошибка создания.", ex);
		}		

		return mTable;
	}

	// Создание колонок таблицы надо вызывать отдельно, чтобы был уже привязан
	// обработчик событий от таблицы.
	public void createTableColumns() {
		mTable.createTableColumns();
	}

	@Override
	protected CommonToolbar createToolbar() {
		JournalToolbar toolbar = new JournalToolbar(getSession(), this, SWT.NONE, null);
		toolbar.addToolBarListener(new ToolbarListener() {
			
			@Override
			public void toolBarButtonPressed(Button button) {
				try {
					if (button.getData().toString().equals("create")) {
						((CommonListControllerListener)getControllerListener()).create();
					} else if (button.getData().toString().equals("edit")) {
						((CommonListControllerListener)getControllerListener()).edit();
					} else if (button.getData().toString().equals("delete")) {
						((CommonListControllerListener)getControllerListener()).delete();
					}
				} catch (DTOException | InitException ex) {
					getControllerListener().sendError("Ошибка операции.", ex);
					return;
				}
			}
		});
		return toolbar;
	}

	@Override
	public void selectById(String id) throws DTOException {
		mTable.selectById(id);
	}

	@Override
	public void selectByDTO(CommonDTO dto) {
		mTable.selectByDTO(dto);
	}

	public CommonTable getTable() {
		return mTable;
	}

}
