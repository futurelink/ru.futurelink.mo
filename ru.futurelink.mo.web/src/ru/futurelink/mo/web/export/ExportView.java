/**
 * 
 */
package ru.futurelink.mo.web.export;

import java.io.InputStream;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.dto.CommonDTOList;
import ru.futurelink.mo.orm.security.User;
import ru.futurelink.mo.web.controller.CommonControllerListener;

/**
 * @author pavlov
 *
 */
public abstract class ExportView {
	
	private CommonDTOList<? extends CommonDTO> 	mList;
	private CommonControllerListener				mControllerListener;
	private User									mUser;

	public ExportView(User user) {
		mUser = user;
	}
	
	final public User getUser() {
		return mUser;
	}
	
	final public void setDTO(CommonDTOList<? extends CommonDTO> dto) {
		mList = dto;
	}

	final public CommonDTOList<? extends CommonDTO> getDTO() {
		return mList;
	}

	/**
	 * Set report export controller listener for report view.
	 * 
	 * @param listener
	 */
	final public void addControllerListener(CommonControllerListener listener) {
		mControllerListener = listener;
	}

	/**
	 * Get report controller listener from report view.
	 * 
	 * @return
	 */
	final public CommonControllerListener getControllerListener() {
		return mControllerListener;
	}
	
	public String getMimeType() {
		return "text/html; charset=utf-8";
	}

	public String getContentDisposition() {
		return "filename=\"report.html\"";
	}
	
	/**
	 * Get contents of a report.
	 * 
	 * @return
	 */
	public abstract InputStream getContents();
}
