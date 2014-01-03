package hr.fer.zemris.jcms.web.navig.builders.course.assessments.schedule;

import hr.fer.zemris.jcms.service.has.HasAssessment;
import hr.fer.zemris.jcms.service.has.HasCourseInstance;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.ActionNavigationItem;
import hr.fer.zemris.jcms.web.navig.Navigation;
import hr.fer.zemris.jcms.web.navig.NavigationBuilder;
import hr.fer.zemris.jcms.web.navig.TextNavigationItem;
import hr.fer.zemris.jcms.web.navig.builders.course.assessments.AdminAssessmentViewBuilder;

public class AssessmentScheduleBuilder extends NavigationBuilder {

	public static void build(Navigation navig, AbstractActionData actionData, boolean root) {
		HasCourseInstance dc = (HasCourseInstance)actionData;
		
		navig.suggestPageTitle(actionData.getMessageLogger().getText("Navigation.courseHome") + " " +dc.getCourseInstance().getCourse().getName());
		AdminAssessmentViewBuilder.build(navig, actionData, false);
		if(root) {
			navig.getNavigationBar("m2")
				.addItem(
					new TextNavigationItem("Navigation.assessments.schedule.editing", true)
				);
		} else {
			navig.getNavigationBar("m2")
				.addItem(
					new ActionNavigationItem("Navigation.assessments.schedule.editing", true, "AssessmentSchedule")
					.addParameter("assessmentID", ((HasAssessment)actionData).getAssessment().getId())
				);
		}
	}

}
