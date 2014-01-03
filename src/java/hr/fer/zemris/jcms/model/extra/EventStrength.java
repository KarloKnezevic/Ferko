package hr.fer.zemris.jcms.model.extra;

/**
 * Opis jakosti događaja.
 * 
 * @author marcupic
 *
 */
public enum EventStrength {
	/**
	 * Jak događaj. Obavezno ga poštivati prilikom rada sa studentkim zauzećima (primjerice,
	 * treba napraviti raspored međuispita - koje događaje tada ne smijemo gaziti?).
	 * Ovo će biti primjerice zauzeća predavanjima.
	 */
	STRONG,
	/**
	 * Srednje jak događaj. Može ga se poštivati prilikom rada sa studentkim zauzećima,
	 * ali i ne mora.
	 */
	MEDIUM,
	/**
	 * Slab događaj. Ne uzima se u obzir prilikom rada sa studentskim zauzećima.  
	 */
	WEAK
}
