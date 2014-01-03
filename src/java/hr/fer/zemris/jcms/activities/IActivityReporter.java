package hr.fer.zemris.jcms.activities;

/**
 * <p>Sučelje koje definira ponašanje objekta za prijavu novih aktivnosti.</p>
 * <p>Način uporabe je sljedeći:</p>
 * <ol>
 * <li>Otvori se nov< sjednica pozivom metode {@link #openSession()}.</li>
 * <li>Potreban broj puta doda se aktivnost, pozivom metode {@link #addActivity(Serializable)}.</li>
 * <li>Ako je sve u redu, pozove se {@link #commitAndCloseSession()}, čime se aktivnosti
 *     pohranjuju i omogućava se postupak njihove objave.</li>
 * <li>Ako se želi odustati, dovoljno je pozvati {@link #rollbackAndCloseSession()}.</li>
 * </ol>
 * <p><i>Napomena: </i> sjednice se pohranjuju u kontekst trenutne dretve, što znači da jedna dretva
 * mora svoje poslove obavljati serijski (otvoriti sjednicu, napraviti što treba, zatvoriti sjednicu).
 * Implementacija ovog sučelja mora dozvoliti da više paralelnih dretvi radi paralelno (svaka sa
 * svojom zasebnom sjednicom).</p>
 * 
 * @author marcupic
 */
public interface IActivityReporter {
	
	/**
	 * Otvara novu sjednicu. Sjednica se ne može otvoriti više puta (ugniježđeno otvaranje).
	 */
	public void openSession();
	
	/**
	 * Dodaje aktivnost.
	 * 
	 * @param activity aktivnost koja se dodaje
	 */
	public void addActivity(AbstractActivity activity);
	
	/**
	 * Potvrđuje da su aktivnosti spremne za objavu i zatvara sjednicu.
	 */
	public void commitAndCloseSession();
	
	/**
	 * Poništava sve dodane aktivnosti.
	 */
	public void rollbackAndCloseSession();
	
	/**
	 * Provjerava postoji li u kontekstu trenutne dretve otvorena sjednica.
	 * 
	 * @return <code>true</code> ako postoji, <code>false</code> inače.
	 */
	public boolean isSessionOpen();
}
