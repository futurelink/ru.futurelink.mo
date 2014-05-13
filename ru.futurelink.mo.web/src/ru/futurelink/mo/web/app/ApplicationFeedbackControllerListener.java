/**
 * 
 */
package ru.futurelink.mo.web.app;

import ru.futurelink.mo.web.controller.CommonControllerListener;

/**
 * @author pavlov
 *
 */
public interface ApplicationFeedbackControllerListener extends
		CommonControllerListener {
	public void feedbackButtonClicked();
}
