package hr.fer.zemris.jcms.web.navig.builders.course;

import hr.fer.zemris.jcms.service.has.HasCourseInstance;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.ActionNavigationItem;
import hr.fer.zemris.jcms.web.navig.Navigation;
import hr.fer.zemris.jcms.web.navig.NavigationBuilder;
import hr.fer.zemris.jcms.web.navig.TextNavigationItem;
import hr.fer.zemris.jcms.web.navig.builders.course.assessments.AdminAssessmentListBuilder;

public class StudentScoreBrowserSettingsBuilder extends NavigationBuilder {

	public static void build(Navigation navig, AbstractActionData actionData, boolean root) {
		HasCourseInstance d = (HasCourseInstance)actionData;
		navig.suggestPageTitle(actionData.getMessageLogger().getText("Navigation.courseHome") + " " +d.getCourseInstance().getCourse().getName());
		AdminAssessmentListBuilder.build(navig, actionData, false);
		if(root) {
			navig.getNavigationBar("m2")
				.addItem(
					new TextNavigationItem("Navigation.studentScoreSettings")
			);
		} else {
			navig.getNavigationBar("m2")
				.addItem(
					new ActionNavigationItem("Navigation.studentScoreSettings", "StudentScoreBrowserSelection")
					.addParameter("courseInstanceID", d.getCourseInstance().getId())
		);
		}
	}
	
}
