/*******************************************************************************
 * Copyright (c) 2013-2014 Pavlov Denis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pavlov Denis - initial API and implementation
 ******************************************************************************/

package ru.futurelink.mo.web.composites;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.dialogs.CommonDialog;
import ru.futurelink.mo.web.controller.CommonControllerListener;
import ru.futurelink.mo.web.controller.CompositeParams;
import ru.futurelink.mo.web.controller.iface.IComposite;
import ru.futurelink.mo.web.exceptions.InitException;
import ru.futurelink.mo.web.utils.UTF8Control;

/**
 * Composite widget for any purpose. This class is used in application for any custom controls or
 * container widgets. This class have an access to user session, locale and other features, so
 * it's better to use it.
 *
 * Also CommonComposite gets a structure of CompositeParams which may be used after
 * and before composite creation. Parameters are needed to translate the state or view
 * of composite from composite controller or parent controller for which the composite
 * controller is as subcontroller.
 *
 * @author pavlov
 *
 */
public class CommonComposite 
	extends Composite
	implements IComposite {

	private static final long serialVersionUID = 1L;

	private ApplicationSession session;
	private ResourceBundle	strings;
	private ResourceBundle	localStrings;
	private ResourceBundle	localErrorStrings;
	private ResourceBundle	errorStrings;
	private CommonDialog		ownerDialog;	// Диалоговое окно - если открыто в диалоге
	private CompositeParams		params;

	private CommonControllerListener	mListener;
	private Logger				mLogger;
	
	public CommonComposite(ApplicationSession session, Composite parent, int style, CompositeParams params) {
		super(parent, style  | SWT.NO_FOCUS );

		setData(RWT.CUSTOM_VARIANT, "commonComposite");

		mLogger = LoggerFactory.getLogger(getClass());

		this.params	 = params;
		this.session = session;
		strings = ResourceBundle.getBundle("locale/main", getLocale(), new UTF8Control());
		errorStrings = ResourceBundle.getBundle("locale/errors", getLocale(), new UTF8Control());

		try {
			localStrings = ResourceBundle.getBundle("locale/main", getLocale(), 
				getClass().getClassLoader(), new UTF8Control());
		} catch(Exception e) {
			mLogger.warn(getErrorString("noLocaleStrings"), getLocale().getLanguage());
		}

		try {
			localErrorStrings = ResourceBundle.getBundle("locale/errors", 
				getLocale(), getClass().getClassLoader(), new UTF8Control());
		} catch (Exception e) {
			// Ignore that exception
		}
	}

    /**
     * The Composite constructor is protected to make it impossible to use from outside
     * the package.
     *
     * @param parent
     */
	protected CommonComposite(Composite parent) { 
		super(parent, SWT.NONE);
		
		setData(RWT.CUSTOM_VARIANT, "commonComposite");
	}

    /**
     * Get composite logger object.
     *
     * @return
     */
	public Logger logger() { return mLogger; }
	
	/**
	 * Composite init method.
     *
     * This method does nothing in CommonComposite, but it
     * may be reimplemented to crate user interface controls and
     * so on.
	 * 
	 * @throws InitException 
	 */
	@Override
	public synchronized void init() throws InitException {}
	
	/**
	 * Implemented for convinence. Get user session locale.
	 * 
	 * @return объект локали типа Locale
	 */
	@Override
	final public Locale getLocale() {
		if (session != null) {
			return session.getLocale();
		}
		return null;
	}

	/**
	 * Get user application session.
	 * 
	 * @return объект сессии пользователя ApplicationSession.
	 */
	@Override
	final public ApplicationSession getSession() {
		return session;
	}
	
	/**
	 * <p>Get ResourceBundle to localize strings in composite. Resource is loaded from
	 * resources/locale/main_xx.properties in bundle classpath which lists application strings.</p>
	 * 
	 * <p>The returned object can be used as any ResourceBundle object.</p>
	 * 
	 * @return ResourceBundle loaded for user session
	 */
	@Deprecated
	final public ResourceBundle getStrings() {
		return strings;
	}

    /**
     * <p>The ResourceBundle to localize strings in composite is loaded from
     * resources/locale/main_xx.properties in bundle classpath which lists application
     * strings.</p>
     *
     * <p>This method gets a localized string from composite's resource bundle.</p>
     *
     * @return ResourceBundle loaded for user session
     */
    @Override
	final public String getLocaleString(String stringName) {    	
		if (localStrings != null) {
			try {
				return localStrings.getString(stringName);
			} catch (MissingResourceException e) {
				try {
					return strings.getString(stringName);
				} catch (MissingResourceException e2) {
					return stringName;
				}
			}
		} else {
			try {
				return strings.getString(stringName);
			} catch (MissingResourceException e2) {
				return stringName;
			}
		}
	}

    /**
     * Get error string from resources/locale/errors_xx.properties resource bundle
     * to localize errors.
     * 
     * @param stringName
     * @return
     */
    @Override
    final public String getErrorString(String stringName) {
    	if (localErrorStrings != null) {
			try {
				return localErrorStrings.getString(stringName);
			} catch (MissingResourceException e) {
				try {
					return errorStrings.getString(stringName);
				} catch (MissingResourceException e2) {
					return stringName;
				}
			}
		} else {
			try {
				return errorStrings.getString(stringName);
			} catch (MissingResourceException e2) {
				return stringName;
			}
		}
    }
    
    /**
     * <p>The ResourceBundle to localize strings in composite is loaded from
     * resources/locale/main_xx.properties in bundle classpath which lists application
     * strings.</p>
     *
     * <p>This method gets a single or multiple count word localized string from
     * resource bundle depending from value parameter.</p>
     *
     * @param value
     * @param single
     * @param multiple
     * @return
     */
	@Override
	final public String getLocaleNumeric(Integer value, String single, String multiple) {
		if (value > 1) {
			return String.format(getLocaleString(multiple), value);
		} else {
			return String.format(getLocaleString(single), value);
		}
	}
	
	@Override
	final public void setStrings(ResourceBundle bundle) {
		strings = bundle;
	}
	
	/**
     * <P> Add controller object handler to the composite. Any composite needs handler
     * to communicate with the controller of this composite. </P>
     *
     * <P> In order to create a composite controller handler must override
     * CommonControllerListener interface, or any suitable inherited class and implement it.</P>
     *
     * <P> The method is best cause in createComposite() immediately after composite object creation.
     * A implementation of the interface handler worth to be created in the overridden
     * createControllerListener(). </P>
	 * 
	 * @param listener
	 */
	@Override
	final public void addControllerListener(CommonControllerListener listener) {
		mListener = listener;
	}
	
	/**
	 * Get assigned listener for controller, assigned with the addControllerListener().
	 * 
	 * @return объект CommonControllerListener или любой от него унаследованный
	 */
	@Override
	public CommonControllerListener getControllerListener() {
		if (mListener == null) {
			if (CommonComposite.class.isInstance(getParent())) {
				return ((CommonComposite)getParent()).getControllerListener();
			} else {
				return null;
			}
		}
		return mListener;
	}
	
	/**
	 * <p>When the composite is to be viewed in dialog window it needs the owner dialog
     * to be set. It's for CommonDialog internal methods.</p>
	 * 
	 * <p><i>DON'T use this method in your code!</i></p>
	 * 
	 * @param dialog the dialog object to pack this composite into
	 */
	@Override
	public final void setOwnerDialog(CommonDialog dialog) {
		ownerDialog = dialog;
	}
	
	/**
	 * <p>Вывести результат в диалог владелец композита. Диалог владелец - это такой диалог,
	 * к которому выполнен attachComposite этого композита.</p>
	 * 
	 * <p>Используется для того, чтобы из композита вернуть диалогу результат его работы. Например,
	 * диалоговое окно открывается модально, результат работы из композита передается в диалоговое
	 * окно, чтобы потом забрать его в том контроллере, из которого производился вызов этого
	 * модального окна.</p> 
	 * 
	 * @param result
	 */
	final protected void setOwnerDialogResult(Object result) {
		if (ownerDialog != null)
			ownerDialog.setResult(result);
	}
	
	/**
     * <p>Get CompositeParams paremeter by name from parameter structure passed to composite.
     * Parameters are passed to composite in reimplemented createComposite() method.
     * </p>
	 *
	 * <p>The composite have no access to params() as a controller, but it has a feature to
     * get one parameter which is neede by name.</p>
	 * 
	 * @param paramName parameter name
	 * @return parameter value
	 */
	protected Object getParam(String paramName) {
		if (params != null) 
			return params.get(paramName);
		return null;
	}

}
