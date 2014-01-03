package hr.fer.zemris.jcms.web.navig;

import hr.fer.zemris.jcms.service.has.HasCourseInstance;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;

public class NavigationManager {

	public static void buildMain(Navigation navig, AbstractActionData actionData) {
		navig.getNavigationBar("m0")
			.addItem(
				new ActionNavigationItem("Navigation.home", "Main")
			)
			.addItem(
				new ActionNavigationItem("Navigation.forum", "ForumIndex")
			);
	}

	public static void buildCourseHome(Navigation navig, AbstractActionData actionData) {
		buildMain(navig, actionData);
		HasCourseInstance d = (HasCourseInstance)actionData;
		navig.setPageTitle(d.getCourseInstance().getCourse().getName());
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
