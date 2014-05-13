package ru.futurelink.mo.web.controller;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.web.composites.CommonComposite;

/**
 * Контроллер главного фрейма для отображения всех данных программы.
 * 
 * @author Futurelink
 * @since 1.2
 *
 */
public class MainCompositeController extends CompositeController {
	
	/**
	 * Стандартный конструктор для работы в режиме субконтроллера.
	 * 
	 * @param parentController
	 * @param dataClass
	 * @param container
	 * @param compositeParams
	 */
	public MainCompositeController(CompositeController parentController, Class<? extends CommonObject> dataClass, 
			Composite container, CompositeParams compositeParams) {
		super(parentController, dataClass, container, compositeParams);
	}

	@Override
	protected CommonComposite createComposite(CompositeParams params) {
		CommonComposite c = new CommonComposite(getSession(), getContainer(), SWT.NONE, null);
		c.addControllerListener(createControllerListener());
		c.setLayout(new FillLayout());

		return c;
	}

	@Override
	protected void doAfterCreateComposite() {}

	@Override
	protected void doBeforeCreateComposite() {}

	@Override
	public CommonControllerListener createControllerListener() {
		// Создадим хотя бы простой обработчик, большего не надо.
		return new CommonControllerListener() {			
			@Override
			public void sendError(String errorText, Exception exception) {
				handleError(errorText, exception);
			}
		};
	}

	@Override
	protected void doBeforeInit() {

	}

	@Override
	protected void doAfterInit() {
		
	}

	@Override
	public void refresh(boolean refreshSubcontrollers) throws Exception {
		logger().debug("Вызвано обновление MainCompositeController, обновляем...");
		super.refresh(refreshSubcontrollers);
	}	
	
}
