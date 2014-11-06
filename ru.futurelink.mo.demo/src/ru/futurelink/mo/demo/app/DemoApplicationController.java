package ru.futurelink.mo.demo.app;

import java.util.Map;

import org.eclipse.swt.widgets.Shell;

import ru.futurelink.mo.web.app.ApplicationController;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.controller.CommonControllerListener;
import ru.futurelink.mo.web.controller.CompositeController;
import ru.futurelink.mo.web.controller.CompositeParams;
import ru.futurelink.mo.web.controller.MainCompositeController;

public class DemoApplicationController extends ApplicationController {

	private MainCompositeController	 mainCompositeController;

	public DemoApplicationController(ApplicationSession session, Shell shell) {
		super(session, shell);
	}

	@Override
	protected CommonComposite createComposite(CompositeParams params) {
		DemoApplicationWindow applicationWindow = new DemoApplicationWindow(getSession(), getContainer());
		applicationWindow.addControllerListener(createControllerListener());
		return applicationWindow;
	}

	@Override
	protected void doAfterCreateComposite() {
		// Create workspace subcontroller after the composite was created
		mainCompositeController = new MainCompositeController(this, null, 
			((DemoApplicationWindow)getComposite()).getMainComposite(), null);
		mainCompositeController.setBundleContext(getBundleContext());
		addSubController(mainCompositeController);
	
		super.doAfterCreateComposite();
	}

	@Override
	public CommonControllerListener createControllerListener() {
		return new DemoApplicationControllerListener() {
			@Override
			public void sendError(String errorText, Exception exception) {
				handleError(errorText, exception);
			}

			@Override
			public CompositeController runUsecase(String usecaseName, Class<?> dataClass) {
				return handleRunUsecase(usecaseName, true);
			}

			@Override
			public void refresh() throws Exception {
				mThisController.refresh(true);
			}

			@Override
			public void navigate(String tag, Map<String, Object> params) {
				
			}

			@Override
			public void testEvent() {
				handleTestEvent();
			}
		};
	}

	@Override
	public CompositeController handleRunUsecase(String usecaseBundle, boolean clearBeforeRun) {
		// When the application controller gets run usecase method executed
		// we redirect it to main composite controller so as it run use case itself
		return mainCompositeController.handleRunUsecase(usecaseBundle, clearBeforeRun);
	}

	public void handleTestEvent() {
		((DemoApplicationControllerListener)getControllerListener()).testEvent();
	}
}
