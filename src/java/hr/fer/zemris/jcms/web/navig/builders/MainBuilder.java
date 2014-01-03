package hr.fer.zemris.jcms.web.navig.builders;

import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.Navigation;
import hr.fer.zemris.jcms.web.navig.NavigationBuilder;

/**
 * Builder linkova na početnoj stranici. Za sada generira samo početnu traku.
 * 
 * @author marcupic
 */
public class MainBuilder extends NavigationBuilder {

	public static void build(Navigation navig, AbstractActionData actionData, boolean root) {
		MainBuilderPart.build(navig, actionData, root);
	}
	
}
