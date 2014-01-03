package hr.fer.zemris.jcms.web.actions;

import javax.persistence.EntityManager;

import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;
import hr.fer.zemris.jcms.web.navig.Navigation;
import hr.fer.zemris.jcms.web.support.TransactionalMethodSupport;

/**
 * <p>Proširenje razreda {@link ExtendedActionSupport}. Dodana je mogućnost
 * automatskog stvaranja i povezivanja odgovarajućeg podatkovnog objekta
 * ({@link #data}, mogućnost izrade navigacije stranice te mogućnost automatske
 * uporabe podatkovnih transakcija koje mogu biti žive i tijekom renderiranja
 * JSP-ova, pa više ne očekujemo probleme oko iznimki tipa <code>LazyInitializationException</code>.</p>
 *  
 * <p>Akcije koje koriste ovaj razred MORAJU ga anotirati uporabom {@link WebClass}.
 * Također, metode koje izvodi struts2 kao odgovor na korisnikove zahtjeve (<code>execute</code> ili
 * ono što je definirano kao naziv metode u URL-u) moraju biti anotirane uporabom
 * {@link WebMethodInfo}.</p>
 * 
 * <p>Izvođenje akcije tada se izvodi na sljedeći način.</p>
 * <ol>
 *  <li>Prije bilo čega od strane struts2, stvara se primjerak razreda određenog
 *      anotacijom {@link WebClass#dataClass()} i postavlja u akciju.</li>
 *  <li>Otvara se podatkovna transakcija. <code>EntityManager</code> se stavlja
 *      korisniku na raspolaganje pozivom metode {@link #getEntityManager()}.</li>
 *  <li>Ako je traženo u anotaciji metode {@link WebMethodInfo#loginCheck()},
 *      provjerava se je li korisnik prijavljen. Ako nije, preskače se pozivanje 
 *      metode, a rezultat postavlja na {@link ExtendedActionSupport#NOT_LOGGED_IN}.</li>
 *  <li>U podatke akcije se automatski popunjavaju podatci o trenutno prijavljenom
 *      korisniku, koji time postaju dostupni pozivom {@link AbstractActionData#getCurrentUser()}.</li>
 *  <li>Inicijalizira se security manager ({@link JCMSSecurityManagerFactory}), pa
 *      to nije potrebno raditi ručno.</li>
 *  <li>Prepušta se struts2-u da pokrene dalje seriju interceptora i konačno same
 *      metode koja je zadana (<code>execute</code> ili neka druga). Ovdje se može
 *      koristiti strutsova validacija i druge mogućnosti. Važno za zapamtiti je da
 *      ako se dogodi greška kod strutsove validacije, naša se metoda opet ne poziva
 *      i kao rezultat se postavlja tekst "input". Naravno, kako smo sada pod transakcijom,
 *      ovo dalje možemo mapirati na što god želimo. Također, sada imamo mogućnost
 *      generirati sve što je potrebno za navigaciju, ukoliko je to moguće (ovisi o
 *      vrsti pogreške). <b>Važno</b>: objekte koje koristite nemojte pohranjivati
 *      direktno u akciju, već ih pohranite u podatke (data).</li>
 *  <li>Nakon što se pozvana metoda izvrši, rezultat koji se dobije pozivom
 *      <code>getData().getResult()</code> mapira se u rezultat koji se vraća
 *      struts2-u. Točno mapiranje može se podesiti odgovarajućim anotacijama na
 *      razini pozvane metode; ukoliko tamo nije zadano što treba napraviti, provjerava
 *      se još popis defaultnih mapiranja.</li>
 *  <li>Nakon prethodne točke znamo što vraćamo struts2-u (temeljem čega će on dalje
 *      odrediti idemo li na JSP, novu akciju ili što već). Sada kada znamo koji je točno
 *      prikaz odabran traži se builder navigacije za dotični prikaz. Najprije se traži
 *      u anotaciji same metode ({@link WebMethodInfo#struts2ResultMappings()}). Ako se tamo
 *      ne pronađe, gleda se defaultni builder zadan u anotaciji razreda ({@link WebClass#defaultNavigBuilder()}).
 *      Ako se niti tada ne pronađe, pokušava se vidjeti postoji li builder čije je ime
 *      identično imenu akcije i  ima nastavak "Builder". Ako se akcija nalazi u nekom
 *      podpaketu paketa "hr.fer.zemris.jcms.web.actions2", tada se builder traži u odgovarajućem
 *      podpaketu buildera. Kada se pronađe odgovarajući builder, poziva se njegova metoda
 *      <code>build</code>.</li>
 *  <li>Ako je u anotaciji metode postavljen {@link WebMethodInfo#transactionalMethod()#closeImmediately()},
 *      ili ako je isti postavljen kod mapiranja prikaza u builder, transakcija se odmah zatvara.</li>
 *  <li>Pušta se struts2 da odradi generiranje rezultata; ako je rezultat mapiran u JSP, iscrtava se
 *      JSP. Ako je mapiran u stream, stream se vraća korisniku, itd. Uočimo da ako nismo tražili
 *      eksplicitno zatvaranje transkacije, u JSP-u će nam raditi Lazy Loading. Međutim, ako akcija
 *      vraća neki stream (datoteku, sliku i sl), tada OBAVEZNO tražite zatvaranje transakcije jer
 *      ona više nije potrebna.</li>
 *  <li>Konačno, kada je sve gotovo (JSP je nacrtan i vraćen korisniku), ako još nije, transakcija se
 *      zatvara i otpuštaju se svi resursi.</li>
 * </ol>
 * 
 * @author marcupic
 *
 * @param <DT> tip podatkovnog objekta
 */
public class Ext2ActionSupport<DT extends AbstractActionData> extends ExtendedActionSupport {

	private static final long serialVersionUID = 1L;
	
	public static final String NO_NAVBUILDER_FOUND = "nonavbuilder";
	
	protected DT data;
	private Navigation navigation = new Navigation();
	
	private static final Class<?>[] constructorParams = new Class<?>[] {IMessageLogger.class};

	/**
	 * Dohvaćanje podatkovnog objekta.
	 * 
	 * @return podatkovni objekt
	 */
	public DT getData() {
		return data;
	}
	
	/**
	 * Dohvaćanje entity managera.
	 * 
	 * @return entity manager
	 */
	public EntityManager getEntityManager() {
		return TransactionalMethodSupport.getEntityManager();
	}
	
	/**
	 * Postavljanje podatkovnog objekta. Ovo radi sam framework, pa ne pozivati eksplicitno.
	 * 
	 * @param data podatkovni objekt
	 */
	public void setData(DT data) {
		this.data = data;
	}
	
	/**
	 * Dohvaćanje navigacijskog objekta.
	 * 
	 * @return navigacijski objekt
	 */
	public Navigation getNavigation() {
		return navigation;
	}
	
	/**
	 * Stvaranje podatkovnog objekta. Zove sam framework, pa ne dirati!
	 */
	@SuppressWarnings("unchecked")
	public void constructData() {
		if(data==null) {
			WebClass dc = this.getClass().getAnnotation(WebClass.class);
			if(dc==null) {
				throw new IllegalArgumentException("Nedostaje anotacija WebClass.");
			}
			IMessageLogger logger = MessageLoggerFactory.createMessageLogger(this, true);
			Class<?> cl = dc.dataClass();
			try {
				data = (DT)cl.getConstructor(constructorParams).newInstance(new Object[] {logger});
			} catch(Throwable ex) {
				ex.printStackTrace();
				throw new IllegalArgumentException("Ne mogu stvoriti primjerak razreda "+cl+".");
			}
		}
	}
}
