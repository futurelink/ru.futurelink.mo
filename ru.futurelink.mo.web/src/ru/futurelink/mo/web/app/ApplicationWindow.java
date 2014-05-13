package ru.futurelink.mo.web.app;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.web.composites.FullscreenComposite;
import ru.futurelink.mo.web.controller.CompositeParams;

abstract public class ApplicationWindow extends FullscreenComposite {
	
	private static final long serialVersionUID = 1L;
	
	public ApplicationWindow(ApplicationSession session, Composite parent) {
		super(session, parent, SWT.NONE, new CompositeParams());
   
		setData(RWT.CUSTOM_VARIANT, "applicationWindow");
		//setBackgroundMode(SWT.INHERIT_DEFAULT);

		createMainWindow();
	}
	
	protected abstract void createMainWindow();

}
