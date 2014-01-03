package hr.fer.zemris.jcms.service.assessments;

import hr.fer.zemris.jcms.service.assessments.defimpl.DynaCodeEngine;

public class DynaCodeEngineFactory {
	
	private static final IDynaCodeEngine engine = new DynaCodeEngine();
	
	public static IDynaCodeEngine getEngine() {
		return engine;
	}
	
}
