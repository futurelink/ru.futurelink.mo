package ru.futurelink.mo.web.composites;

import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.orm.dto.CommonDTO;
import ru.futurelink.mo.orm.exceptions.DTOException;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.fields.IField;
import ru.futurelink.mo.web.composites.toolbar.CommonToolbar;
import ru.futurelink.mo.web.controller.CompositeParams;

/**
 * Composite to work with single data item via EditorDTO.
 * 
 * @author pavlov
 *
 */
public class CommonItemComposite extends CommonDataComposite {

	private static final long serialVersionUID = 1L;

	private CommonDTO		mDTO;
	private IField[]		mMandatoryFields;
	
	public CommonItemComposite(ApplicationSession session, Composite parent, int style, CompositeParams params) {
		super(session, parent, style, params);
	}

	public void refresh() throws DTOException {}
	
	/**
	 * Link EditorDTO object to composite.
	 * 
	 * @param data
	 */
	public void attachDTO(CommonDTO data, Boolean refresh) throws DTOException {
		mDTO = data;
		if ((mDTO != null) && (refresh == true))
			refresh();
	}

	/**
	 * Unlink and clear linked EditorDTO.
	 */
	public void removeDTO() {
		if (mDTO != null)
			mDTO.clear();
		mDTO = null;
	}

	/**
	 * Convinience method to make possible not to cast DTO to
     * CommonDTO.
	 */
	public CommonDTO getDTO() {
		return mDTO;
	}

	/**
     * Make "Save" button enabled or disable it. Needs to be reimplemented
     * on composite with this button.
     *
	 * @param enabled
	 */
	public void setSaveEnabled(boolean enabled) {}
	
	/**
     * Make "Revert" button enabled or disable it. Needs to be reimplemented
     * on composite with this button.
	 * 
	 * @param enabled
	 */
	public void setRevertEnabled(boolean enabled) {}

	@Override
	protected void createContents() {
		createCaptionLabel();
		mWorkspace = createWorkspace();		
		mToolbar = createToolbar();
	}

    /**
     * Workspace creation method. Reimplement in subclasses, to workspace
     * is created by default.
     *
     * @return
     */
	@Override
	protected CommonComposite createWorkspace() {
		return null;
	}

    /**
     * Toolbar creation method. Reimplement in subclasses, no toolbar
     * is created by default.
     *
     * @return
     */
	@Override
	protected CommonToolbar createToolbar() {
		return null;
	}

	/**
	 * Check if all mandatory fields are filled with values.
	 * 
	 * @return
	 */
	public boolean getIsMandatoryFilled() {
		if (mMandatoryFields != null)
			for (IField field : mMandatoryFields) {
				if (field.isEmpty()) return false;
			}
		return true;
	}
	
	/**
	 * Set mandatory fields list for item form. This list
     * is set once at form creation and then fields from this
     * list cannot be declared as non-mandatory.
     *
	 * @param fields
	 */
	protected void setMandatoryFields(IField[] fields) {
		if (mMandatoryFields != null) {
			for (IField field : mMandatoryFields) {
				field.setMandatory(false);
			}			
		}

		mMandatoryFields = fields;
		for (IField field : fields) {
			field.setMandatory(true);
		}
	}
}
