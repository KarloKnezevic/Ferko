package hr.fer.zemris.jcms.web.navig.builders.course.assessments;

import hr.fer.zemris.jcms.service.has.HasAssessment;

import hr.fer.zemris.jcms.service.has.HasCourseInstance;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.ActionNavigationItem;
import hr.fer.zemris.jcms.web.navig.Navigation;
import hr.fer.zemris.jcms.web.navig.NavigationBuilder;
import hr.fer.zemris.jcms.web.navig.TextNavigationItem;

public class AssessmentViewBuilder extends NavigationBuilder {

	public static void build(Navigation navig, AbstractActionData actionData, boolean root) {
		HasCourseInstance d = (HasCourseInstance)actionData;
		HasAssessment a = (HasAssessment)actionData;
		
		navig.suggestPageTitle(actionData.getMessageLogger().getText("Navigation.courseHome") + " " +d.getCourseInstance().getCourse().getName());
		AssessmentSummaryViewBuilder.build(navig, actionData, false);
		if(root) {
			navig.getNavigationBar("m2")
				.addItem(
					new TextNavigationItem(a.getAssessment().getName(), false)
			);
		} else {
			navig.getNavigationBar("m2")
				.addItem(
					new ActionNavigationItem(a.getAssessment().getName(), false, "AssessmentView")
					.addParameter("courseInstanceID", d.getCourseInstance().getId())
					.addParameter("assessmentID", a.getAssessment().getId())
					.addParameter("userID", actionData.getCurrentUser().getId())
			);
		}
	}

}
