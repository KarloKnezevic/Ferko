package hr.fer.zemris.jcms.web.actions.annotations;

import hr.fer.zemris.jcms.web.navig.DefaultNavigationBuilder;
import hr.fer.zemris.jcms.web.navig.NavigationBuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotacija koja mapira odabrani prikaz na željeno transakcijsko ponašanje i željeni
 * navigacijski builder.
 * 
 * @author marcupic
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Struts2ResultMapping {
	/**
	 * Što je odabrano kao prikaz? Temeljem ovog stringa i mapiranja same akcije u jcms.xml odabire
	 * se u konačnici tip rezultata. Za ovu anotaciju to je ulazni parametar.
	 *  
	 * @return odabrani prikaz
	 */
	String struts2Result();
	/**
	 * Koji se navigacijski builder koristi? Za ovu anotaciju to je izlazni parametar.
	 * 
	 * @return builder navigacije
	 */
	Class<? extends NavigationBuilder> navigBuilder() default DefaultNavigationBuilder.class;
	/**
	 * Kakvo je transakcijsko ponašanje obzirom na odabrani prikaz? Za ovu anotaciju to je izlazni parametar.
	 * 
	 * @return transakcijsko ponašanje
	 */
	TransactionalMethod transactionalMethod() default @TransactionalMethod(closeImmediately=false);
	/**
	 * Da li se navigacijski builder poziva sa root=<code>true</code> ili <code>false</code>?
	 * 
	 * @return željeni način pozivanja
	 */
	boolean navigBuilderIsRoot() default true;
	/**
	 * <p>Nakon što se pozove navigacijski builder, što još i
	 * gdje treba dodati?
	 * Polje koje se ovdje predaje mora biti parne veličine. Pri tome se na lokaciji <code>2*i</code> očekuje
	 * naziv izbornika u koji se stavka dodaje, a na lokaciji <code>2*i+1</code> ili tekst stavke koji treba
	 * dodati, ili naziv property-ja u <code>data</code> objektu čiju vrijednost treba dohvatiti i postaviti
	 * kao vrijednost stavke. Ako tekst započinje znakom ljestvi, tumači se kao naziv property-ja čiju vrijednost
	 * treba dohvatiti; inače se tumači kao ključ. Ako se zada property, on se razrješava obzirom na objekt 
	 * <code>data</code>.</p>
	 * 
	 * <p>Primjer:<br>
	 * <code>{"m2","AssessmentFlags.nav.importValues", "m2", "#assessmentFlag.name"}</code></p>
	 * 
	 * @return polje dodatnih izborničkih stavki
	 */
	String[] additionalMenuItems() default {};
}
