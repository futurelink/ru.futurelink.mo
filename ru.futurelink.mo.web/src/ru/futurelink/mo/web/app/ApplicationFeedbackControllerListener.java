/**
 * 
 */
package ru.futurelink.mo.web.app;

import ru.futurelink.mo.web.controller.CommonControllerListener;

/**
 * Application feedback button controller listener.
 *
 * @author pavlov
 *
 */
public interface ApplicationFeedbackControllerListener extends
		CommonControllerListener {
	public void feedbackButtonClicked();
}
