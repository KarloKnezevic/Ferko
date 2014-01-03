package hr.fer.zemris.jcms.service.assessments;

/**
 * Sučelje koje služi za odgođeni dohvat podataka o tome što su
 * studenti uploadali i koliko su bodova na tome dobili.
 * 
 * @author marcupic
 *
 */
public interface IOnDemandStudentTaskCallback {
	/**
	 * Kada se zada kratko ime komponente (npr. LAB), i pozicija (npr. 1),
	 * vraća podatke za sve što je do tada uploadano i ocijenjeno.
	 * 
	 * @param componentShortName kratko ime komponente
	 * @param itemPosition pozicija komponente
	 * @return podaci o uploadu na item
	 */
	public TaskData getAllStudentTaskData(String componentShortName, int itemPosition);
}
