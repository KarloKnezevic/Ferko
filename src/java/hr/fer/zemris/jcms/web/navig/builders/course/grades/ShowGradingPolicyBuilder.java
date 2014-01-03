package hr.fer.zemris.jcms.web.navig.builders.course.grades;

import hr.fer.zemris.jcms.service.has.HasCourseInstance;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.ActionNavigationItem;
import hr.fer.zemris.jcms.web.navig.Navigation;
import hr.fer.zemris.jcms.web.navig.NavigationBuilder;
import hr.fer.zemris.jcms.web.navig.TextNavigationItem;
import hr.fer.zemris.jcms.web.navig.builders.course.CourseBuilderPart;

public class ShowGradingPolicyBuilder extends NavigationBuilder {

	public static void build(Navigation navig, AbstractActionData actionData, boolean root) {
		HasCourseInstance d = (HasCourseInstance)actionData;
		navig.suggestPageTitle(actionData.getMessageLogger().getText("Navigation.courseHome") + " " +d.getCourseInstance().getCourse().getName());
		CourseBuilderPart.build(navig, actionData, false);
		navig.getNavigationBar("m2")
			.addItem(
				new ActionNavigationItem(d.getCourseInstance().getCourse().getName(), false, "ShowCourse")
				.addParameter("courseInstanceID", d.getCourseInstance().getId())
			);
		if(root) {
			navig.getNavigationBar("m2")
				.addItem(
					new TextNavigationItem("Navigation.gradingPolicy")
			);
		} else {
			navig.getNavigationBar("m2")
				.addItem(
					new ActionNavigationItem("Navigation.gradingPolicy", true, "ShowGradingPolicy")
					.addParameter("courseInstanceID", d.getCourseInstance().getId())
			);
		}
	}

}
