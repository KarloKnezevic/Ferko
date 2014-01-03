package hr.fer.zemris.jcms.web.actions.data.support;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import hr.fer.zemris.jcms.locking.LockPath;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.service.has.HasCurrentUser;


/**
 * Apstraktni roditelj za sve podatke akcija.
 * 
 * @author marcupic
 *
 */
public abstract class AbstractActionData implements HasCurrentUser {

	/**
	 * Koristi se za prijenos parametra za zaključavanje.
	 */
	private String lid;
	
	/**
	 * Koristiti ako je akcija izvedena korektno.
	 */
	public static final String RESULT_SUCCESS = "success";
	/**
	 * Koristiti ako je izvođenje akcije završilo u grešci, ali je prikupljeno dovoljno podataka
	 * da se može izrenderirati normalna navigacija.
	 */
	public static final String RESULT_NONFATAL_ERROR = "nonFatalError";
	/**
	 * Koristiti ako korisnik nema dozvole.
	 * Edit: ovo izbjegavati jer nista ne govori o tome može li se sljedeća stranica
	 * normalno izrenderirati ili ne. Bolji pristup je u poruke ubaciti odgovarajucu poruku,
	 * i vratiti {@link #RESULT_FATAL}. Stovise, ovdje cak niti ne znamo je li korisnik
	 * logiran, i nema dozvole, ili uopce nije logiran!
	 */
	@Deprecated
	public static final String RESULT_NO_PERMISSION = "noPermission";
	/**
	 * Koristiti ako se od korisnika očekuje da dopuni podatke. Ovo podrazumjeva da se
	 * kompletna navigacija može normalno restaurirati.
	 */
	public static final String RESULT_INPUT = "input";
	/**
	 * Koristiti ako se od korisnika očekuje potvrda. Ovo podrazumjeva da se
	 * kompletna navigacija može normalno restaurirati.
	 */
	public static final String RESULT_CONFIRM = "confirm";
	/**
	 * Koristiti ako se je došlo u situaciju gdje su čak i osnovni
	 * parametri neispravni, pa se ne mogu pripremiti niti objekti
	 * potrebni za prikaz normalne navigacije. U ovom slučaju korisniku
	 * bi trebalo prikazati samo poruku o pogrešci i ponuditi mu link
	 * natrag na naslovnicu. 
	 */
	public static final String RESULT_FATAL = "fatal";

	/**
	 * Koristiti ako postoji mogućnost da korisnik osim onoga što je htio napraviti napravi nešto što 
	 * ne bi smio. Npr. želi obrisati kategoriju, a u njoj se još nalaze datoteke i/ili druge podkategorije.
	 * U ovom slučaju korisnika se vraća na prethodnu stranicu, ali      
	 */
	public static final String RESULT_REVIEW_ACTION = "reviewAction";
	
	
	
	private LockPath lockPath;
	private User currentUser;
	private IMessageLogger messageLogger;
	private String result;
	
	/**
	 * Konstruktor.
	 * 
	 * @param messageLogger podrška za i18n
	 */
	public AbstractActionData(IMessageLogger messageLogger) {
		super();
		this.messageLogger = messageLogger;
	}

	/**
	 * Dohvaća stazu po kojoj je obavljeno zaključavanje.
	 * Rezultat će biti <code>null</code> ako zaključavanje
	 * nije napravljeno.
	 * 
	 * @return stazu ili <code>null</code>
	 */
	public LockPath getLockPath() {
		return lockPath;
	}
	
	/**
	 * Postavlja stazu po kojoj je napravljeno zaključavanje. Ovo će tipično
	 * postaviti sam framework. Može biti i <code>null</code> ako se ništa nije
	 * zaključalo.
	 * 
	 * @param lockPath staza ili <code>null</code> 
	 */
	public void setLockPath(LockPath lockPath) {
		this.lockPath = lockPath;
	}
	
	/**
	 * Vraća message logger.
	 * @return
	 */
	public IMessageLogger getMessageLogger() {
		return messageLogger;
	}
	
	/**
	 * Vraća postavljeni rezultat.
	 * 
	 * @return postavljeni rezultat
	 */
	public String getResult() {
		return result;
	}
	
	/**
	 * Postavlja rezultat usluge. Preddefinirane vrijednosti koje se
	 * mogu koristiti su {@link #SUCCESS}, {@link #NO_PERMISSION} te 
	 * {@link #INPUT}; međutim, svaka usluga slobodna je definirati
	 * i neke druge vrijednosti (ako ih dokumentira, tako da autor akcije
	 * koja koristi uslugu zna što može očekivati od usluge).
	 * 
	 * @param result vrijednost temeljem koje će pozivatelj odlučiti što dalje
	 */
	public void setResult(String result) {
		this.result = result;
	}
	
	/**
	 * Vraća trenutnog korisnika.
	 * @return trenutni korisnik
	 */
	public User getCurrentUser() {
		return currentUser;
	}
	public void setCurrentUser(User currentUser) {
		this.currentUser = currentUser;
	}
	
	/**
	 * Parametar za zaključavanja (ako se koristi). Služi za one akcije
	 * koje temeljem njega grade LockPath.
	 * @param lid parametar za zaključavanje
	 */
	public void setLid(String lid) {
		this.lid = lid;
	}
	public String getLid() {
		return lid;
	}

	private DecimalFormat[] formats = null;
	
	/**
	 * Formatira broj na 4 decimale. Ova metoda nije thread-safe, ali zato cacheira jednom
	 * stvoreni formatter unutar ovog podatkovnog objekta. To je obicno dobro za pozivanje 
	 * iz OGNL-a kod iscrtavanja jedne stranice gdje se treba napraviti puno formatiranja.
	 * 
	 * @param d broj
	 * @return broj formatirani na 4 decimale
	 */
	public String dfScore(double d) {
		return df(d, 4);
	}
	
	/**
	 * Formatira broj na zadani broj decimala. Ova metoda nije thread-safe, ali zato cacheira jednom
	 * stvoreni formatter unutar ovog podatkovnog objekta. To je obicno dobro za pozivanje 
	 * iz OGNL-a kod iscrtavanja jedne stranice gdje se treba napraviti puno formatiranja. Postavljeno
	 * je ograničenje da se broj može prikazivati sa nula do šest decimala.
	 * 
	 * @param d broj
	 * @param n broj decimala
	 * @return broj formatiran na traženi broj decimala
	 */
	public String df(double d, int n) {
		if(n<0) n=0;
		if(n>6) n=6;
		if(formats==null) {
			formats = new DecimalFormat[n+1];
		} else if(formats.length<n+1) {
			DecimalFormat[] f2 = new DecimalFormat[n+1];
			System.arraycopy(formats, 0, f2, 0, formats.length);
			formats = f2;
		}
		if(formats[n]==null) {
			StringBuilder sb = new StringBuilder(n+5);
			if(n==0) {
				sb.append('#');
			} else {
				sb.append("#.");
				for(int i = 0; i < n; i++) {
					sb.append('0');
				}
			}
			DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(Locale.ENGLISH);
			DecimalFormat f = new DecimalFormat(sb.toString(), dfs);
			f.setRoundingMode(RoundingMode.HALF_UP);
			formats[n] = f;
		}
		DecimalFormat f = formats[n];
		return f.format(d);
	}
	private SimpleDateFormat shortDateFormat;
	private SimpleDateFormat fullDateFormat;
	
	private SimpleDateFormat getShortDateFormat() {
		if(shortDateFormat==null) {
			shortDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		}
		return shortDateFormat;
	}
	private SimpleDateFormat getFullDateFormat() {
		if(fullDateFormat==null) {
			fullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
		return fullDateFormat;
	}
	/**
	 * Pomoćna metoda koja obavlja formatiranje datuma po formatu "yyyy-MM-dd".
	 * 
	 * @param date datum koji treba formatirati
	 * @return formatirani datum
	 */
	public String formatDate(Date date) {
		return date==null ? "" : getShortDateFormat().format(date);
	}
	/**
	 * Pomoćna metoda koja obavlja formatiranje datuma i vremena po formatu "yyyy-MM-dd HH:mm:ss".
	 * 
	 * @param date datum koji treba formatirati
	 * @return formatirani datum i vrijeme
	 */
	public String formatDateTime(Date date) {
		return date==null ? "" : getFullDateFormat().format(date);
	}

}
