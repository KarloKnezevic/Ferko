package hr.fer.zemris.jcms.web.navig.builders.course;

import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.Navigation;
import hr.fer.zemris.jcms.web.navig.NavigationBuilder;

public class ShowCourseBuilder extends NavigationBuilder {

	public static void build(Navigation navig, AbstractActionData actionData, boolean root) {
		CourseBuilderPart.build(navig, actionData, root);
	}
	
}
