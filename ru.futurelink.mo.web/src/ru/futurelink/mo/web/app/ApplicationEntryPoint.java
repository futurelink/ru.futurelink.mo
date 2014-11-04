package ru.futurelink.mo.web.app;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.EntryPoint;
import org.eclipse.rap.rwt.client.service.JavaScriptLoader;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.BundleContext;

/**
 * Application entry point.
 * 
 * @author pavlov
 *
 */
abstract public class ApplicationEntryPoint implements EntryPoint {
	private ApplicationSession			mSession;
	private ApplicationWindow		mApplicationWindow;
	private ApplicationController	mController;
	private String						mDeferredUsecaseRun;
	private BundleContext				mBundleContext;

	private static String fontCSSJS = "fontcss.js";

	public ApplicationEntryPoint(BundleContext context) {
		mSession = new ApplicationSession(context);
		mBundleContext = context;
	}

	/**
	 * Get application session.
	 * 
	 * @return
	 */
	public ApplicationSession getSession() {
		return mSession;
	}
	
	/**
	 * Set an application controller.
	 * 
	 * @see getController
	 * @param controller
	 */
	protected void setController(ApplicationController controller) {
		mController = controller;
	}
	
	/**
	 * Get an application controller.
	 * 
	 * @see setController
	 * @return
	 */
	protected ApplicationController getController() {
		return mController;
	}

	public BundleContext getBundleContext() {
		return mBundleContext;
	}

	/**
	 * Из точки входа в приложение можно запустить юзкейс, но этот
	 * юзкейс должен быть особенным, ему не должно требоваться работать с данными.
	 * 
	 * Юзкейс будет запущен как раз после того, как создастся окно приложения.
	 * 
	 * @param usecaseBundle
	 */
	public void runDeferredUsecase(String usecaseBundle) {
		mDeferredUsecaseRun = usecaseBundle;
	}

	/**
	 * Метод вызывается после создания интерфейса приложения.
	 * При определении нужно явно вызвать его после создания главного окна в createUI().
	 */
	protected void doAfterUICreation() {
		// Запустить отложенный юзкейс
		if (mDeferredUsecaseRun != null) {
			if (mController != null)
				mController.handleRunUsecase(mDeferredUsecaseRun, false);
		}
	}
	
	/**
	 * Set application composite window for application entry point.
	 * 
	 * @param window application composite window
	 */
	protected void setApplicationWindow(ApplicationWindow window) {
		mApplicationWindow = window;
	}

	/**
	 * Removes and disposes application composite window.
	 * 
	 * @see setApplicationWindow
	 */
	protected void removeApplicationWindow() {
		//mApplicationWindow.mController.uninit();
		mApplicationWindow.dispose();
		mApplicationWindow = null;
	}

	private void registerFontCSS() throws IOException {
		InputStream inputStream = ApplicationEntryPoint.class.getClassLoader().getResourceAsStream(fontCSSJS);
		try {
			if (inputStream != null)
				RWT.getResourceManager().register(fontCSSJS, inputStream);
		} finally {
			if (inputStream != null)
				inputStream.close();
	    }		
	}
	
	/**
	 * Creates shell for application entry point.
	 * 
	 * @see Shell
	 * @param display
	 * @return shell object
	 */
	protected Shell createMainShell( Display display ) {
		try {
			registerFontCSS();

			JavaScriptLoader executor = RWT.getClient().getService(JavaScriptLoader.class);
			executor.require(RWT.getResourceManager().getLocation(fontCSSJS));
		} catch (IOException ex) {
			// Failed to load font CSS javascript...
		}

		display.setData( RWT.MNEMONIC_ACTIVATOR, "CTRL+ALT" );
	    display.addListener( SWT.Resize, new Listener() {
			private static final long serialVersionUID = 1L;

			public void handleEvent( Event event ) {
	    	    System.out.println( "Display size: " + event.width + "x" + event.height );
	    	  }
	    	} );
	    
		Shell shell = new Shell(display, SWT.NO_TRIM);
	    shell.setMaximized(true);
	    shell.setData(RWT.CUSTOM_VARIANT, "mainshell");
	    shell.setLayout(new FillLayout());
   
	    return shell;
	}

	/**
	 * Disposes all controls in shwll window and completely clears it.
	 * 
	 * @param shell
	 */
	protected void clearShell(Shell shell) {
		Control[] controls = shell.getChildren();
		for (int i = 0; i < controls.length; i++) {
			if(controls[i] != null) {
				controls[i].dispose();
			}
		}
	}
}
