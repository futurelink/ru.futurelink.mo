package ru.futurelink.mo.web.topmenu;

import org.eclipse.swt.widgets.Composite;

import ru.futurelink.mo.orm.CommonObject;
import ru.futurelink.mo.web.app.ApplicationSession;
import ru.futurelink.mo.web.composites.CommonComposite;
import ru.futurelink.mo.web.controller.CompositeController;
import ru.futurelink.mo.web.controller.CompositeParams;

abstract public class TopMenuController extends CompositeController {

	public TopMenuController(ApplicationSession session,
			Class<? extends CommonObject> dataClass) {
		super(session, dataClass);
	}

	public TopMenuController(CompositeController parentController, Class<? extends CommonObject> dataClass, 
			Composite container, CompositeParams compositeParams) {
		super(parentController, dataClass, container, compositeParams);
	}

	@Override
	protected CommonComposite createComposite(CompositeParams params) {
		return null;
	}

	@Override
	protected void doBeforeCreateComposite() {
	}

	@Override
	protected void doAfterCreateComposite() {
	}

	@Override
	protected void doBeforeInit() {
	}

	@Override
	protected void doAfterInit() {	
	}

	@Override
	public void processUsecaseParams() {
	}
}
