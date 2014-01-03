package hr.fer.zemris.jcms.web.navig.builders;

import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.ActionNavigationItem;
import hr.fer.zemris.jcms.web.navig.Navigation;
import hr.fer.zemris.jcms.web.navig.NavigationBuilder;

/**
 * Pomoć za konstrukciju buildera početne stranice. Gradi linkove na samom
 * vrhu stranice. Ovo je temeljna navigacija sustava koja je prisutna u svim
 * "pogledima".
 * 
 * @author marcupic
 */
public class MainBuilderPart  extends NavigationBuilder {

	public static void build(Navigation navig, AbstractActionData actionData, boolean root) {
		navig.suggestPageTitle(actionData.getMessageLogger().getText("Navigation.main"));
		navig.getNavigationBar("m0")
		.addItem(
			new ActionNavigationItem("Navigation.home", "Main")
		)
		.addItem(
			new ActionNavigationItem("Navigation.forum", "ForumIndex")
		)
		.addItem(
			new ActionNavigationItem("Navigation.calendar", "SimpleCalendar")
		);
	}
	
}
