/**
 * 
 */
package ru.futurelink.mo.web.controller.iface;

import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;

/**
 * @author pavlov
 *
 */
public interface ICompositeController 
	extends IController, ISessionDecorator, IBundleDecorator 
{
	public Composite getComposite();
	public Composite getContainer();
	
	public void processUsecaseParams();
	
	public void reparentComposite(Composite newParent);

	public void clear();
	public void refreshBySender(String sender, boolean refreshSubcontrollers) throws Exception;
	public void addDropSupport(int operations, Transfer[] transferTypes, DropTargetListener listener);
	public ICompositeController getParentController();
}
