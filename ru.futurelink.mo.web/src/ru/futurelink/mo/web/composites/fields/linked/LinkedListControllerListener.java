/**
 * 
 */
package ru.futurelink.mo.web.composites.fields.linked;

import ru.futurelink.mo.web.controller.CommonControllerListener;

/**
 * @author pavlov
 *
 */
public interface LinkedListControllerListener extends CommonControllerListener {
	public void autoCompleteSelected(String selectionText);
	public void autoCompleteEntered();
	public void selectNext();
	public void selectPrev();
}
