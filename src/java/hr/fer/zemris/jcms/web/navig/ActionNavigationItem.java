package hr.fer.zemris.jcms.web.navig;

/**
 * Jedan item u navigacijskoj traci koji predstavlja neku akciju.
 * 
 * @author marcupic
 */
public class ActionNavigationItem extends NavigationItem {

	private String actionName;
	private String actionMethod;
	
	/**
	 * @param titleKey ključ koji definira tekst stavke
	 * @param actionName naziv akcije koja se poziva
	 * @param actionMethod naziv metode koju treba pozvati; može biti null
	 */
	public ActionNavigationItem(String titleKey, String actionName, String actionMethod) {
		super("action", titleKey);
		this.actionName = actionName;
		this.actionMethod = actionMethod;
	}

	/**
	 * @param titleKey ključ koji definira tekst stavke
	 * @param actionName naziv akcije koja se poziva
	 */
	public ActionNavigationItem(String titleKey, String actionName) {
		this(titleKey, actionName, "execute");
	}

	/**
	 * @param titleKey ključ koji definira tekst stavke
	 * @param isKey je li predani naslov ključ
	 * @param actionName naziv akcije koja se poziva
	 * @param actionMethod naziv metode koju treba pozvati; može biti null
	 */
	public ActionNavigationItem(String titleKey, boolean isKey, String actionName, String actionMethod) {
		super("action", titleKey, isKey);
		this.actionName = actionName;
		this.actionMethod = actionMethod;
	}

	/**
	 * @param titleKey ključ koji definira tekst stavke
	 * @param isKey je li predani naslov ključ
	 * @param actionName naziv akcije koja se poziva
	 */
	public ActionNavigationItem(String titleKey, boolean isKey, String actionName) {
		this(titleKey, isKey, actionName, "execute");
	}

	/**
	 * Naziv akcije koju treba pozvati.
	 * @return naziv
	 */
	public String getActionName() {
		return actionName;
	}
	
	/**
	 * Metoda u akciji koju treba pozvati.
	 * @return naziv metode
	 */
	public String getActionMethod() {
		return actionMethod;
	}
	
	@Override
	public String toString() {
		return "NavItem key="+actionName+", method="+actionMethod;
	}
}
