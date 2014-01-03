package hr.fer.zemris.jcms.web.navig.builders.course.assessments;

import hr.fer.zemris.jcms.service.has.HasCourseInstance;
import hr.fer.zemris.jcms.web.actions.data.AdminAssessmentFlagEditData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.Navigation;
import hr.fer.zemris.jcms.web.navig.NavigationBuilder;
import hr.fer.zemris.jcms.web.navig.TextNavigationItem;
import hr.fer.zemris.util.StringUtil;

public class AdminAssessmentFlagEditBuilder extends NavigationBuilder {

	public static void build(Navigation navig, AbstractActionData actionData, boolean root) {
		HasCourseInstance d = (HasCourseInstance)actionData;
		
		navig.suggestPageTitle(actionData.getMessageLogger().getText("Navigation.courseHome") + " " +d.getCourseInstance().getCourse().getName());
		AdminAssessmentListBuilder.build(navig, actionData, false);
		
		AdminAssessmentFlagEditData eData = (AdminAssessmentFlagEditData)actionData;
		boolean newFlag = StringUtil.isStringBlank(eData.getBean().getId());

		if(newFlag) {
			navig.getNavigationBar("m2")
				.addItem(
					new TextNavigationItem("AssessmentFlags.definingNew", true)
				);
		} else {
//			navig.getNavigationBar("m2")
//				.addItem(
//					new ActionNavigationItem(((HasAssessment)actionData).getAssessment().getName(), false, "AdminAssessmentView")
//					.addParameter("assessmentID", eData.getBean().getId())
//				);
			navig.getNavigationBar("m2")
				.addItem(
					new TextNavigationItem("AssessmentFlags.editing", true)
				);
		}
	}

}
