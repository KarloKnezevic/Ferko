package hr.fer.zemris.jcms.locking;

/**
 * <p>LockManager služi za obavljanje vanjske sinkronizacije između
 * različitih akcija koje mijenjaju podatke. Ideja je da se zaključavanje
 * obavi <b>prije</b> no što se započnu podatkovne transakcije
 * tako da s time ne bude nikakvih problema.</p>
 * 
 * <p>Kod dodjeljivanja lockova hijerarhijski bliži zahtjevu korijenu imaju
 * veći prioritet i njima će najprije biti dodjeljen lock. Također, lockovi
 * imaju vlasnika (dretvu) pa nije moguće da jedna dretva zaključa stazu
 * a neka druga je otključa.</p>
 *
 * <p>U Ferku se definira sljedeće stablo zaključavanja:</p>
 * <ul>
 * <li>
 *   ml - master lock; kompletan ekskluzivni pristup svemu.
 *   <ul><li>
 *     ciN - lock primjerka kolegija n, gdje je n identifikator kolegija
 *     <ul><li>
 *       g - lock svih grupa na primjerku kolegija
 *       <ul><li>
 *         gN - lock grupeN na primjerku kolegija (ovo mora biti grupa koja je ujedno burza)
 *       </li></ul>
 *     </li><li>
 *       a - lock ocjena i svih provjera i zastavica
 *       <ul><li>
 *         aN - lock provjere (assessment) čiji je identifikator N
 *       </li><li>
 *         fN - lock zastavice čiji je identifikator N
 *       </li></ul>
 *     </li></ul>
 *   </li></ul>
 * </li>
 * </ul>
 * 
 * <h2>Primjeri</h2>
 * <p>Da bismo zaključali sve grupe na kolegiju s identifikatorom 17, koristimo stazu
 * <code>ml\ci17\g</code>.<br>
 * Da bismo zaključali grupu s identifikatorom 10 na kolegiju s identifikatorom 17, koristimo stazu
 * <code>ml\ci17\g\g10</code>. Pretpostavka je da je to burza.<br>
 * Ako želimo zaključati neku grupu koja nije burza, moramo pronaći njezinu burzu i
 * po burzu obaviti zaključavanje (dakle, potrebno je zadati taj identifikator). Samo
 * na taj način možemo imati garancije da će se zaključavanje grupa obaviti korektno. U suprotnom
 * bismo uživo trebali otkrivati hijerarhiju grupa, a to bez baze nije moguće; kako ovo treba napraviti
 * prije dolaska do baze, malo discipline je prihvatljiv kompromis. Ako je iz nekog
 * razloga ipak nemoguće otkriti o kojoj se grupi točno radi, uvijek je moguće
 * zatražiti zaključavanje svih grupa na kolegiju.
 * </p>
 * 
 * @author marcupic
 */
public interface ILockManager {
	/**
	 * Zahtjev za zaključavanjem predane staze.
	 * 
	 * @param lockPath staza
	 */
	public void acquireLock(LockPath lockPath);
	/**
	 * Zahtjev za otključavanjem staze.
	 * 
	 * @param lockPath staza
	 * @throws UnlockException ako otključavanje nije moguće
	 */
	void releaseLock(LockPath lockPath) throws UnlockException;
}
