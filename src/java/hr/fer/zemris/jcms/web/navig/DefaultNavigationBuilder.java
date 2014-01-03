package hr.fer.zemris.jcms.web.navig;

import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;

/**
 * Builder na koji se propada ako ništa nije definirano kako spada.
 * U principu, ne radi ništa.
 * 
 * @author marcupic
 *
 */
public class DefaultNavigationBuilder extends NavigationBuilder {

	public static void build(Navigation navig, AbstractActionData actionData, boolean root) {
		navig.suggestPageTitle("Ups!");
	}
	
}
