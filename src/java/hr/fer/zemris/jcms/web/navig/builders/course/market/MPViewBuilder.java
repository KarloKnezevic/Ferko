package hr.fer.zemris.jcms.web.navig.builders.course.market;

import hr.fer.zemris.jcms.service.has.HasCourseInstance;
import hr.fer.zemris.jcms.service.has.HasParent;

import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.ActionNavigationItem;
import hr.fer.zemris.jcms.web.navig.Navigation;
import hr.fer.zemris.jcms.web.navig.NavigationBuilder;
import hr.fer.zemris.jcms.web.navig.TextNavigationItem;

public class MPViewBuilder extends NavigationBuilder {

	public static void build(Navigation navig, AbstractActionData actionData, boolean root) {
		HasCourseInstance d = (HasCourseInstance)actionData;
		HasParent p = (HasParent)actionData;
		navig.suggestPageTitle(actionData.getMessageLogger().getText("Navigation.courseHome") + " " +d.getCourseInstance().getCourse().getName());
		MPGroupsListBuilder.build(navig, actionData, false);
		
		if(root) {
			navig.getNavigationBar("m2")
				.addItem(
					new TextNavigationItem(p.getParent().getName(),false)
			);
		} else {
			navig.getNavigationBar("m2")
				.addItem(
					new ActionNavigationItem(p.getParent().getName(), false, "MPView")
					.addParameter("courseInstanceID", d.getCourseInstance().getId())
					.addParameter("parentID", p.getParent().getId())
			);
		}
	}

}
