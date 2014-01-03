package hr.fer.zemris.jcms.web.actions.annotations;

import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotacija sadrži osnovne podatke o izvođenju metode. 
 * 
 * @author marcupic
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WebMethodInfo {
	/**
	 * Mora li korisnik biti prijavljen na sustav da bi mogao pokrenuti
	 * zadanu akciju? Ako je vrijednost <code>true</code>, sustav će prije
	 * izvođenja same metode automatski obaviti provjeru.
	 * 
	 * @return <code>true</code> ako korisnik mora prijavljen, <code>false</code> inače
	 */
	boolean loginCheck() default true;
	/**
	 * Podešavanje transakcijskog ponašanja metode.
	 * 
	 * @return opis transakcijskog ponašanja
	 */
	TransactionalMethod transactionalMethod() default @TransactionalMethod;
	/**
	 * Mapiranje rezultata koji sloj usluge upisuje u {@link AbstractActionData#setResult(String)},
	 * i rezultata koji se vraća struts-u.
	 *  
	 * @return polje mapiranih rezultata
	 */
	DataResultMapping[] dataResultMappings() default {};
	/**
	 * Mapiranje strutsovog rezultata i potrebne navigacije.
	 * 
	 * @return mapiranje navigacije
	 */
	Struts2ResultMapping[] struts2ResultMappings() default {};
	/**
	 * Koju stazu treba zaključati prije izvođenja same metode? Default je niti jednu.
	 * Staza može biti template-izirana u smislu da nije definirana do kraja,već da
	 * u njoj postoje parametri. Takva staza je oblika:<br>
	 * <code>ml\ci${courseInstanceID}\g\g${groupID}</code><br>
	 * ukoliko je staza takvog oblika, navedeni parametri uzet će se iz parametara
	 * koje je poslao korisnik. Ukoliko se zamjena ne uspije obaviti (jer takvog parametra
	 * nema ili je prazan), izvođenje metode će se preskočiti uz generiranu poruku o pogrešci.
	 * Staza po kojoj je obavljeno zaključavanje u podatkovni objekt će se upisati pozivom
	 * metode <code>setLockPath(LockPath path);</code> ukoliko takva metoda postoji.
	 * 
	 * @return stazu koju treba zaključati
	 */
	String lockPath() default "";
}
