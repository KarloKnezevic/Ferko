package hr.fer.zemris.jcms.model.extra;

/**
 * Status sobe koja sudjeluje u provjeri znanja.
 * Inicijalno, svaka je soba {@link #UNCHECKED}.
 * Pokretanjem akcije provjeri dostupnost status će
 * se promijeniti u jedan od: {@link #UNAVAILABLE}, {@link #AVAILABLE},
 * {@link #NOT_UNDER_CONTROL}.
 * 
 * Pokretanjem akcije "rezerviraj" sve sobe koje su u statusu {@link #UNCHECKED}
 * ili {@link #AVAILABLE} pokušat će se rezervirati.
 * 
 * @author marcupic
 *
 */
public enum AssessmentRoomStatus {
	/**
	 * Status sobe jos nije provjeren.
	 */
	UNCHECKED,
	/**
	 * Soba je u to vrijeme nedostupna.
	 */
	UNAVAILABLE,
	/**
	 * Soba je raspoloživa za rezervaciju.
	 */
	AVAILABLE,
	/**
	 * Soba nije pod kontrolom sustava za rezervacije.
	 */
	NOT_UNDER_CONTROL,
	/**
	 * Soba je rezervirana kroz sustav rezervacija za provjeru.
	 */
	RESERVED,
	/**
	 * Soba je ručno rezervirana (dogovor telefonom i sl).
	 */
	MANUALLY_RESERVED
}
