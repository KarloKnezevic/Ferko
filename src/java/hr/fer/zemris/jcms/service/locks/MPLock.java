package hr.fer.zemris.jcms.service.locks;

/**
 * Baza podataka podesena je tako da koristi transakcijski model REPEATABLE_READ.
 * Medutim, dio operacija koje radimo s burzom zahtjeva SERIALIZABLE. Nazalost,
 * ovaj model nije moguce dinamicki mijenjati (to je rezultat kombinacije 
 * Hibernate+Connection Pooling). Stoga za operacije koje MIJENJAJU stanje na burzi
 * koristimo ovaj razred koji nudi vanjsko zakljucavanje. Treba napomenuti da ce
 * ovo postati problem ako se odluci krenuti u distribuciju same web aplikacije na
 * vise posluzitelja, i tada kao implementaciju treba koristiti nesto sto ce raditi
 * u distribuiranom slucaju.
 * 
 * @author marcupic
 *
 */
public interface MPLock {
	/**
	 * Zakljucaj marketplace. Predaje se id grupe, ne burze!
	 * 
	 * @param marketPlaceGroupID
	 */
	public void writeLock(Long marketPlaceGroupID);
	/**
	 * Otkljucaj marketplace. Predaje se id grupe, ne burze!
	 * 
	 * @param marketPlaceGroupID
	 */
	public void releaseLock(Long marketPlaceGroupID);
}
