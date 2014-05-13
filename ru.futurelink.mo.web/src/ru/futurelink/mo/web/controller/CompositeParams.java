package ru.futurelink.mo.web.controller;

import java.util.HashMap;

public class CompositeParams {
	
	private HashMap<String, Object> mParams;
	
	public CompositeParams() {
		mParams = new HashMap<String, Object>();
	}
	
	public CompositeParams add(String paramName, Object paramValue) {
		mParams.put(paramName, paramValue);
		return this;
	}
	
	public Object get(String paramName) {
		return mParams.get(paramName);
	}
}
