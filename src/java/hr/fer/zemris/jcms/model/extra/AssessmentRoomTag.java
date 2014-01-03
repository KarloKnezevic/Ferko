package hr.fer.zemris.jcms.model.extra;

/**
 * Poželjnost prostorije prilikom automatske izrade rasporeda.
 * 
 * @author marcupic
 *
 */
public enum AssessmentRoomTag {
	/**
	 * Ovu prostoriju sustav ne smije koristiti.
	 */
	FORBIDDEN,
	/**
	 * Poželjnost ove prostorije je niska.
	 */
	LOW_DESIREABILITY,
	/**
	 * Poželjnost ove prostorije je srednja.
	 */
	MEDIUM_DESIREABILITY,
	/**
	 * Poželjnost ove prostorije je visoka.
	 */
	HIGH_DESIREABILITY,
	/**
	 * Ovu prostoriju sustav treba obavezno koristiti.
	 */
	MANDATORY
}
