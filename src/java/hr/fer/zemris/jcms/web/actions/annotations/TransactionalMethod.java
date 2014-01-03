package hr.fer.zemris.jcms.web.actions.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotacija koja se koristi za opisivanje transakcijskog ponašanja.
 * 
 * @author marcupic
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TransactionalMethod {
	/**
	 * Treba li zatvoriti transakciju odmah po izlasku iz akcije? Ako je
	 * <code>false</code>, transakcija će biti otvorena sve dok se ne izgenerira
	 * i kompletan rezultat, i dok se on ne dostavi klijentu.
	 * 
	 * @return <code>true</code> ako se transakcija zatvara odmah, <code>false</code> inače
	 */
	boolean closeImmediately() default false;
}
