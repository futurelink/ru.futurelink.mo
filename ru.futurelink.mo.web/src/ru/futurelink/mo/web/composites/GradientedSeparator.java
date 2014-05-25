/**
 * 
 */
package ru.futurelink.mo.web.composites;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @author pavlov
 *
 */
public class GradientedSeparator extends Composite {
	private static final long serialVersionUID = 1L;

	/**
	 * @param parent
	 * @param style
	 */
	public GradientedSeparator(Composite parent, int style) {
		super(parent, style | SWT.NO_FOCUS);
		
		setData(RWT.CUSTOM_VARIANT, "gradientedSeparator");
		setBackgroundMode(SWT.INHERIT_NONE);
	}

}
