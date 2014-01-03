package hr.fer.zemris.jcms.activities;

/**
 * Sučelje preko kojega se dolazi do jedne konkretne implementacije podsustava
 * za rad s aktivnostima. Razredi koji implementiraju ovo sučelje nude dvije metode:
 * {@link #getActivityReporter()} za dohvat objekta preko kojega se prijavljuju
 * aktivnosti, te {@link #getActivityWorker()} za dohvat objekta preko kojeg Ferko
 * upravlja tim podsustavom. Ova dva objekta su povezana na način da reporter na
 * odgovarajući način interno prilikom commita aktivnosti workeru dojavljuje da su
 * aktivnosti spremne za objavu, nakon čega worker obavlja objavu.
 * 
 * @author marcupic
 *
 */
public interface IActivityServiceProvider {
	/**
	 * Dohvat objekta za prijavu aktivnosti.
	 * 
	 * @return objekt za prijavu aktivnosti
	 */
	public IActivityReporter getActivityReporter();
	/**
	 * Dohvat objekta za upravljanje objavama.
	 * 
	 * @return objekt koji objavljuje aktivnosti
	 */
	public IActivityWorker getActivityWorker();
}
