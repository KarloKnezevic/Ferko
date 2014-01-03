package hr.fer.zemris.jcms.periodicals;

/**
 * Sučelje koje opisuje proširenja sustava koja
 * se periodički pokreću.
 * 
 * @author marcupic
 */
public interface IPeriodicalService {
	/**
	 * Metoda kojom se podsustavu predaje poruka (key,value).
	 * 
	 * @param key ključ
	 * @param value vrijednost
	 */
	public void passMessage(String key, String value);
	/**
	 * Metoda koja se poziva na početku kako bi se obavila
	 * inicijalizacija podsustava. 
	 */
	public void init();
	/**
	 * Metoda koja se poziva na kraju i koja zahtjeva od sustava
	 * da uništi sve otvorene resurse, dretve i slično.
	 */
	public void destroy();
	/**
	 * Metoda koja pokreće izvođenje akcije podsustava.
	 */
	public void periodicalExecute();
}
