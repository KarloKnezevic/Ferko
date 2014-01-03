package hr.fer.zemris.jcms.service.assessments;

import hr.fer.zemris.jcms.service.assessments.defimpl.ScoreCalculatorEngine;

/**
 * Factory razred za stvaranje engine-a. Ovo je zapravo implementirano kao Singleton pattern.
 *  
 * @author marcupic
 *
 */
public class ScoreCalculatorEngineFactory {

	private static final IScoreCalculatorEngine engine = new ScoreCalculatorEngine();
	
	/**
	 * Vraća primjerak engine-a. Metoda se smije zvati iz više dretvi istovremeno.
	 * 
	 * @return primjerak engine-a
	 */
	public static IScoreCalculatorEngine getEngine() {
		return engine;
	}
	
}
