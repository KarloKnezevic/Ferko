package hr.fer.zemris.jcms.activities;

/**
 * Sučelje predstavlja ugovor između Ferka i implementacije sustava
 * za objavu događaja. Ferko će na početku rada pozvati metodu
 * {@link #start()} kojom se treba inicijalizirati sve potrebno kako
 * bi podsustav mogao normalno raditi. Na kraju rada, Ferko će pozvati
 * metodu {@link #stop()} kojom se treba sve pogasiti.
 * 
 * @author marcupic
 *
 */
public interface IActivityWorker {
	/**
	 * Metoda kojom se pokreće implementacija IActivityWorker-a.
	 */
	public void start();
	/**
	 * Metoda kojom se zaustavlja implementacija IActivityWorker-a.
	 */
	public void stop();
}
