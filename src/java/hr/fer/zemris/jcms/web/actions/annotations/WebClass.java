package hr.fer.zemris.jcms.web.actions.annotations;

import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.navig.DefaultNavigationBuilder;
import hr.fer.zemris.jcms.web.navig.NavigationBuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Anotacija koja parametrizira razrede izvedene iz {@link Ext2ActionSupport}.
 * Omogućava podešavanje razreda koji će se automatski stvoriti i postaviti nad
 * objektom akcije tipa {@link Ext2ActionSupport} (konkretno, radi se o pozivu metode
 * {@link Ext2ActionSupport#setData(hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData)}.</p>
 * 
 * <p>Anotacija je nužna nad svim razredima koji su izvedeni iz {@link Ext2ActionSupport}.</p>
 * 
 * @author marcupic
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WebClass {
	/**
	 * Podešavanje razreda čiji će se primjerak stvoriti automatski, i postaviti
	 * u akciju pozivom metode
	 * {@link Ext2ActionSupport#setData(hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData)}. 
	 * 
	 * @return razred podatkovnog objekta
	 */
	Class<?> dataClass();
	
	/**
	 * Podešavanje razreda koji predstavlja defaultni builder za navigacijski podsustav.
	 * Ovaj builder će se koristiti ako se na razini same metode ne pronađe specifičniji
	 * builder.
	 * 
	 * @return builder za navigaciju 
	 */
	Class<? extends NavigationBuilder> defaultNavigBuilder() default DefaultNavigationBuilder.class;
	
	/**
	 * Ako se koristi navigacijski builder zadan property-jem {@link #defaultNavigBuilder()}, da li ga se
	 * poziva sa root=<code>true</code> ili <code>false</code>?
	 * 
	 * @return željeni način pozivanja
	 */
	boolean defaultNavigBuilderIsRoot() default true;
	
	/**
	 * <p>Nakon što se pozove defaultni navigacijski builder (ako je podešeno da se koristi), što još i
	 * gdje treba dodati? Ako uporaba defaultnog navigacijskog buildera nije podešena, tada se ovo ignorira.
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
