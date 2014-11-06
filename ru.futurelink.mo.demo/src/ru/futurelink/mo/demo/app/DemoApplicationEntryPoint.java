package ru.futurelink.mo.demo.app;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.BundleContext;

import ru.futurelink.mo.web.app.ApplicationEntryPoint;

public class DemoApplicationEntryPoint extends ApplicationEntryPoint {
	private Shell shell;

	public DemoApplicationEntryPoint(BundleContext context) {
		super(context);
	}

	@Override
	public int createUI() {
		Display display = new Display();
		shell = createMainShell(display);
		shell.setBackgroundMode(SWT.INHERIT_DEFAULT);

		try {
			setApplicationWindow(new DemoApplicationWindow(getSession(), shell));
			doAfterUICreation();
		} catch (Exception ex) {
			MessageDialog.openError(shell, "Error", ex.toString());
		}

		shell.open();
		while( !shell.isDisposed() ) {
			if( !display.readAndDispatch() ) {
				display.sleep();
			}
		}

		display.dispose();

		return 0;
	}
}
