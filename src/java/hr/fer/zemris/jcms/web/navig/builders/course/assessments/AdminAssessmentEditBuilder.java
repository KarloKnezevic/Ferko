package hr.fer.zemris.jcms.web.navig.builders.course.assessments;

import hr.fer.zemris.jcms.service.has.HasAssessment;
import hr.fer.zemris.jcms.service.has.HasCourseInstance;
import hr.fer.zemris.jcms.web.actions.data.AdminAssessmentEditData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.ActionNavigationItem;
import hr.fer.zemris.jcms.web.navig.Navigation;
import hr.fer.zemris.jcms.web.navig.NavigationBuilder;
import hr.fer.zemris.jcms.web.navig.TextNavigationItem;
import hr.fer.zemris.util.StringUtil;

public class AdminAssessmentEditBuilder extends NavigationBuilder {

	public static void build(Navigation navig, AbstractActionData actionData, boolean root) {
		HasCourseInstance d = (HasCourseInstance)actionData;
		
		navig.suggestPageTitle(actionData.getMessageLogger().getText("Navigation.courseHome") + " " +d.getCourseInstance().getCourse().getName());
		AdminAssessmentListBuilder.build(navig, actionData, false);
		
		AdminAssessmentEditData eData = (AdminAssessmentEditData)actionData;
		boolean newAssessment = StringUtil.isStringBlank(eData.getBean().getId());

		if(newAssessment) {
			navig.getNavigationBar("m2")
				.addItem(
					new TextNavigationItem("Assessments.definingNew", true)
				);
		} else {
			navig.getNavigationBar("m2")
				.addItem(
					new ActionNavigationItem(((HasAssessment)actionData).getAssessment().getName(), false, "AdminAssessmentView")
					.addParameter("assessmentID", eData.getBean().getId())
				);
			navig.getNavigationBar("m2")
				.addItem(
					new TextNavigationItem("Assessments.editing", true)
				);
			
		}
	}

}
