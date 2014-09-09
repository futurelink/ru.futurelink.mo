package ru.futurelink.mo.web.app;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.web.composites.FullscreenComposite;
import ru.futurelink.mo.web.controller.CompositeParams;

/**
 * <p>Main application window.</p>
 * 
 * <p>This abstract class is to be implemented to create fullscreen main
 * application windows.</p>
 * 
 * <p>Simply implement @see createMainWindow method to create application window
 * controls.</p>
 * 
 * @author pavlov
 *
 */
abstract public class ApplicationWindow extends FullscreenComposite {
	
	private static final long serialVersionUID = 1L;
	
	public ApplicationWindow(ApplicationSession session, Composite parent) {
		super(session, parent, SWT.NONE, new CompositeParams());
   
		setData(RWT.CUSTOM_VARIANT, "applicationWindow");
		createMainWindow();
	}
	
	protected abstract void createMainWindow();

}
