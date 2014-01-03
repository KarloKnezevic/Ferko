package hr.fer.zemris.jcms.web.navig.builders.course.assessments;

import hr.fer.zemris.jcms.service.has.HasCourseInstance;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.Navigation;
import hr.fer.zemris.jcms.web.navig.NavigationBuilder;
import hr.fer.zemris.jcms.web.navig.TextNavigationItem;

public class AdminAssessmentSummaryViewBuilder extends NavigationBuilder {

	public static void build(Navigation navig, AbstractActionData actionData, boolean root) {
		HasCourseInstance d = (HasCourseInstance)actionData;
		
		navig.suggestPageTitle(actionData.getMessageLogger().getText("Navigation.courseHome") + " " +d.getCourseInstance().getCourse().getName());
		AdminAssessmentListBuilder.build(navig, actionData, false);
		
		navig.getNavigationBar("m2")
			.addItem(
				new TextNavigationItem("Navigation.adminScoreSummaryView", true)
			);
	}

}
