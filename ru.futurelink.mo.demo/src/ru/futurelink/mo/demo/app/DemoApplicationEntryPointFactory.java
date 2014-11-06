package ru.futurelink.mo.demo.app;

import org.eclipse.rap.rwt.application.EntryPoint;
import org.osgi.framework.BundleContext;

import ru.futurelink.mo.web.app.ApplicationEntryPointFactory;

public class DemoApplicationEntryPointFactory extends ApplicationEntryPointFactory {

	public DemoApplicationEntryPointFactory(BundleContext context) {
		super(context);
	}

	@Override
	public EntryPoint create() {
		DemoApplicationEntryPoint mainApp = new DemoApplicationEntryPoint(getBundleContext());
		return mainApp;
	}

}