package ru.futurelink.mo.web.app;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import ru.futurelink.mo.web.composites.dialogs.CommonDialog;
import ru.futurelink.mo.web.controller.CommonController;
import ru.futurelink.mo.web.controller.CompositeController;
import ru.futurelink.mo.web.controller.CompositeParams;
import ru.futurelink.mo.web.exceptions.InitException;
import ru.futurelink.mo.web.register.UseCaseRegister;

/**
 * Контроллер приложения.
 * 
 * @author pavlov
 *
 */
abstract public class ApplicationController extends CompositeController {

	private Shell 				mShell;
	private CommonController	mFeedbackButtonController;

	protected ApplicationController(ApplicationSession session, Shell shell) {
		super(session, null);
		mShell = shell;
		mContainer = shell;
	}

	@Override
	public Composite getContainer() {
		return mShell;
	}

	@Override
	protected void doBeforeCreateComposite() {

	}

	@Override
	protected void doAfterCreateComposite() {

	}

	@Override
	protected void doBeforeInit() {

	}

	@Override
	protected void doAfterInit() throws InitException {
		startFeedback();
	}

	/* (non-Javadoc)
	 * @see ru.futurelink.mo.web.controller.CommonController#uninit()
	 */
	@Override
	public synchronized void uninit() {
		super.uninit();

		// Если есть привязанная кнопка фидбэка, надо
		// ее убрать и очистить контроллер.
		if (mFeedbackButtonController != null) {
			mFeedbackButtonController.uninit();
			mFeedbackButtonController = null;
		}
	}
	
    /** 
     * Тут смотрим, если у нас зарегистирован юзкейс обратной связи, то
     * надо вызвать у него execute('feedbackButton'), чтобы создать кнопку
     * обратной связи.
     */
	private void startFeedback() {
		UseCaseRegister register = (UseCaseRegister) mBundleContext.getService(
				mBundleContext.getServiceReference(UseCaseRegister.class.getName())
				);
		Class<?> feedbackButtonController = 
				register.getController("ru.futurelink.mo.web.feedback.button");
		if (feedbackButtonController != null) {
			try {
				Constructor<?> constr = feedbackButtonController.getConstructor(ApplicationSession.class, Class.class);
				mFeedbackButtonController = (CommonController) constr.newInstance(getSession(), null);
				mFeedbackButtonController.init();
				mFeedbackButtonController.addControllerListener(new ApplicationFeedbackControllerListener() {				
					@Override
					public void sendError(String errorText, Exception exception) {
						handleError(errorText, exception);
					}

					@Override
					public void feedbackButtonClicked() {
						logger().debug("Application feedback button clicked!");
						if (getSession().getMobileMode()) {
							handleRunUsecase("ru.futurelink.mo.web.feedback", null);
						} else {
							CommonDialog dlg = new CommonDialog(getSession(), mShell , SWT.BORDER);
							dlg.setText("Feedback");
							dlg.setSize(CommonDialog.LARGE);
							CompositeController ctrl = getUsecaseController("ru.futurelink.mo.web.feedback", null, dlg.getShell(), new CompositeParams());
							if (ctrl != null) {
								try {
									ctrl.init();

									dlg.attachComposite(ctrl.getComposite());
									dlg.open();
								} catch (InitException ex) {
									ex.printStackTrace();
								}
							}
						}
					}
				});
			} catch (NoSuchMethodException | 
					SecurityException | 
					InstantiationException | 
					IllegalAccessException | 
					IllegalArgumentException | 
					InvocationTargetException | 
					InitException ex) {
				ex.printStackTrace();
			}
		}
	}
}
