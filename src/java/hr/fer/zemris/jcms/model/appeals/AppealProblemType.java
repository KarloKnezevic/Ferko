package hr.fer.zemris.jcms.model.appeals;

/**
 * Tip žalbenog problema.
 * 
 * @author Ivan Krišto
 */
public enum AppealProblemType {
	
	/**
	 * Pogrešno skeniran obrazac na zaokruživanje.
	 * Žalba uz ponuđeni odgovor.
	 */
	BAD_SCAN_OFFER_SOLUTION,
	
	/** Službeno rješenje je loše. */
	WRONG_OFFICIAL_SOLUTION,
	
	/** Potrebno je napraviti izmjene na bodovima za određeni zadatak. */
	SET_SCORE_FOR_PROBLEM,
	
	/** Potrebno je ponovo ispraviti određeni zadatak. */
	CHECK_SCORE_FOR_PROBLEM,
	
	/** Zadatak nije ispravljen. */
	PROBLEM_NOT_EVALUATED,
	
	/** Provjera nije ispravljena. */
	NOT_PROCESSED
}
