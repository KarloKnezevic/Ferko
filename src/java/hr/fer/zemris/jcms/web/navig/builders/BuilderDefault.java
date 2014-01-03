package hr.fer.zemris.jcms.web.navig.builders;

import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.Navigation;
import hr.fer.zemris.jcms.web.navig.NavigationBuilder;

/**
 * Ovaj builder generira samo naslov i glavni izbornik.
 * 
 * @author marcupic
 */
public class BuilderDefault  extends NavigationBuilder {

	public static void build(Navigation navig, AbstractActionData actionData, boolean root) {
		navig.suggestPageTitle("Ups!");
		MainBuilderPart.build(navig, actionData, root);
	}
	
}
