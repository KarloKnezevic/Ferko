package hr.fer.zemris.jcms.web.navig;

/**
 * Jedan item u navigacijskoj traci koji predstavlja običan tekst (dakle, ne
 * da se kliknuti).
 * 
 * @author marcupic
 */
public class TextNavigationItem extends NavigationItem {

	/**
	 * @param titleKey ključ koji definira tekst stavke
	 * @param actionName naziv akcije koja se poziva
	 * @param actionMethod naziv metode koju treba pozvati; može biti null
	 */
	public TextNavigationItem(String titleKeyOrText, boolean isKey) {
		super("text", titleKeyOrText, isKey);
	}

	/**
	 * @param titleKey ključ koji definira tekst stavke
	 */
	public TextNavigationItem(String titleKeyOrText) {
		this(titleKeyOrText, true);
	}

	@Override
	public String toString() {
		return "TextItem key="+getTitleKey();
	}
}
