package hr.fer.zemris.jcms.web.navig.builders.course;

import hr.fer.zemris.jcms.service.has.HasCourseInstance;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.ActionNavigationItem;
import hr.fer.zemris.jcms.web.navig.Navigation;
import hr.fer.zemris.jcms.web.navig.NavigationBuilder;
import hr.fer.zemris.jcms.web.navig.builders.MainBuilder;

public class CourseBuilderPart extends NavigationBuilder {

	public static void build(Navigation navig, AbstractActionData actionData, boolean root) {
		MainBuilder.build(navig, actionData, false);
		HasCourseInstance d = (HasCourseInstance)actionData;
		navig.suggestPageTitle(actionData.getMessageLogger().getText("Navigation.courseHome") + " " +d.getCourseInstance().getCourse().getName());
		navig.getNavigationBar("m1")
			.addItem(
				new ActionNavigationItem("Navigation.courseHome", "ShowCourse")
				.addParameter("courseInstanceID", d.getCourseInstance().getId())
			)
			.addItem(
				new ActionNavigationItem("Navigation.calendar", "ShowCourseEvents")
				.addParameter("courseInstanceID", d.getCourseInstance().getId())
			)
			.addItem(
				new ActionNavigationItem("Navigation.repository", "Repository")
				.addParameter("courseInstanceID", d.getCourseInstance().getId())
			)
			.addItem(
				new ActionNavigationItem("Navigation.forum", "CourseCategory")
				.addParameter("courseInstanceID", d.getCourseInstance().getId())
			);
	}
	
}
