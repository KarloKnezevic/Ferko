package hr.fer.zemris.jcms.model.appeals;

/**
 * Stanje u kojem se može nalaziti žalba.
 * 
 * @author Ivan Krišto
 */
public enum AppealInstanceStatus {
	
	/** Žalba još nije obrađena ili zaključana. */
	OPENED,
	
	/** Prihvaćena u orginalu. Bodovno stanje je izmjenjeno. */
	ACCEPTED,
	
	/** Prihvaćena uz izmjene. Bodovno stanje je izmjenjeno. */
	MODIFIED_ACCEPTED,
	
	/** Odbijena. Bodovno stanje nije dirano. */
	REJECTED,
	
	/** Zaključana od nadležne osobe radi razjašnjenja detalja. */
	LOCKED
}
