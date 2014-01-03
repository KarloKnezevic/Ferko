package hr.fer.zemris.jcms.web.navig.builders.course.assessments;

import hr.fer.zemris.jcms.service.has.HasAssessment;
import hr.fer.zemris.jcms.service.has.HasCourseInstance;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.ActionNavigationItem;
import hr.fer.zemris.jcms.web.navig.Navigation;
import hr.fer.zemris.jcms.web.navig.NavigationBuilder;
import hr.fer.zemris.jcms.web.navig.TextNavigationItem;

public class AdminAssessmentViewBuilder extends NavigationBuilder {

	public static void build(Navigation navig, AbstractActionData actionData, boolean root) {
		HasCourseInstance d = (HasCourseInstance)actionData;
		
		navig.suggestPageTitle(actionData.getMessageLogger().getText("Navigation.courseHome") + " " +d.getCourseInstance().getCourse().getName());
		AdminAssessmentListBuilder.build(navig, actionData, false);
		if(root) {
			navig.getNavigationBar("m2")
				.addItem(
					new TextNavigationItem(((HasAssessment)actionData).getAssessment().getName(), false)
				);
		} else {
			navig.getNavigationBar("m2")
				.addItem(
					new ActionNavigationItem(((HasAssessment)actionData).getAssessment().getName(), false, "AdminAssessmentView")
					.addParameter("assessmentID", ((HasAssessment)actionData).getAssessment().getId())
				);
		}
	}

}
