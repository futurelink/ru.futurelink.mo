package ru.futurelink.mo.web.app;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.BrowserNavigation;
import org.eclipse.rap.rwt.client.service.BrowserNavigationEvent;
import org.eclipse.rap.rwt.client.service.BrowserNavigationListener;
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
 * <p>Basic application controller.</p>
 * 
 * <p>This class is the ancestor for applications implemented with
 * MO framework. The ApplicationController is a standard CompositeController and
 * implements ICompositeController intefface.</p>
 * 
 * <p>The AppliucationControllerListener is used to implement application
 * listener.</p>
 * 
 * @see ApplicationControllerListener
 * @see ICompositeController
 * 
 * @author pavlov
 *
 */
abstract public class ApplicationController extends CompositeController {

	private Shell 				mShell;
	private CommonController	mFeedbackButtonController;
	private BrowserNavigationListener mNavigationListener;

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
	protected void doBeforeCreateComposite() {}

	@Override
	protected void doAfterCreateComposite() {}

	@Override
	protected void doBeforeInit() {}

	@Override
	protected void doAfterInit() throws InitException {
		startFeedback();		
	}

	private void initNavigation() {
		mNavigationListener = new BrowserNavigationListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void navigated(BrowserNavigationEvent arg0) {
				logger().info("Navigated on {}", arg0.getState());
				
				// Parse URL tag and params into Map
				Map<String, Object> params = new LinkedHashMap<String, Object>();
				String state = arg0.getState();
				String[] parts = state.split("\\?");  
				String tag = (parts.length > 0) ? parts[0] : "";
				if (parts.length > 1) {
					String[] pairs = parts[1].split("&");
					for (String pair : pairs) {
						String[] pairSplit = pair.split("=");
						try {
							params.put(
									URLDecoder.decode(pairSplit[0], "UTF-8"), 
									URLDecoder.decode((pairSplit.length > 1) ? pairSplit[1] : "", "UTF-8")
								);
						} catch (UnsupportedEncodingException ex) {
						
						}
					}
				}

				// Call navigation handler
				if (getControllerListener() != null)
					((ApplicationControllerListener)getControllerListener()).navigate(tag, params);				
			}
		};
		BrowserNavigation service = RWT.getClient().getService( BrowserNavigation.class );
		service.addBrowserNavigationListener(mNavigationListener);		
	}

	private void uninitNavigation() {
		BrowserNavigation service = RWT.getClient().getService( BrowserNavigation.class );
		service.removeBrowserNavigationListener(mNavigationListener);
		mNavigationListener = null;
	}
	
	@Override
	protected void finalize() throws Throwable {
		uninitNavigation();

		super.finalize();
	}
	
	@Override
	public synchronized void init() throws InitException {
		initNavigation();

		super.init();
	}
	
	@Override
	public synchronized void uninit() {
		super.uninit();

		uninitNavigation();

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
							handleRunUsecase("ru.futurelink.mo.web.feedback");
						} else {
							CommonDialog dlg = new CommonDialog(getSession(), mShell , SWT.BORDER);
							dlg.setText("Feedback");
							dlg.setSize(CommonDialog.LARGE);
							CompositeController ctrl = getUsecaseController(
									"ru.futurelink.mo.web.feedback", 
									dlg.getShell(), 
									new CompositeParams()
								);
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
	
	@Override
	public void processUsecaseParams() {
		
	}
}
