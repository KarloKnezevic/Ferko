package hr.fer.zemris.jcms.web.navig.builders.course.assessments.types.choice;

import hr.fer.zemris.jcms.service.has.HasAssessment;
import hr.fer.zemris.jcms.web.actions.data.AdminSetDetailedChoiceConfData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.ActionNavigationItem;
import hr.fer.zemris.jcms.web.navig.Navigation;
import hr.fer.zemris.jcms.web.navig.NavigationBuilder;
import hr.fer.zemris.jcms.web.navig.TextNavigationItem;
import hr.fer.zemris.jcms.web.navig.builders.course.assessments.AdminAssessmentViewBuilder;

public class ChoiceEditBuilder extends NavigationBuilder {

	private static String[] views = new String[] {
		"Navigation.assessments.choice.default", "default",
		"Navigation.assessments.choice.forms", "forms",
		"Navigation.assessments.choice.scoring", "scoring",
		"Navigation.assessments.choice.answers", "answers",
		"Navigation.assessments.choice.groups", "groups",
		"Navigation.assessments.choice.plabels", "plabels",
		"Navigation.assessments.choice.pmapping", "pmapping",
		"Navigation.assessments.choice.manip", "manip"
	};
	public static void build(Navigation navig, AbstractActionData actionData, boolean root) {
		HasAssessment d = (HasAssessment)actionData;
		navig.suggestPageTitle(d.getAssessment().getName());
		AdminAssessmentViewBuilder.build(navig, actionData, false);

		if(root) {
			navig.getNavigationBar("m2")
				.addItem(
					new TextNavigationItem("Navigation.assessmentsParams")
			);
		} else {
			navig.getNavigationBar("m2")
				.addItem(
					new ActionNavigationItem("Navigation.assessmentsParams", true, "AdminSetDetailedChoiceConf")
					.addParameter("assessmentID", d.getAssessment().getId())
			);
		}

		String view = ((AdminSetDetailedChoiceConfData)actionData).getSelectedView();
		int n = views.length/2;
		for(int i=0; i<n; i++) {
			String key = views[2*i];
			String keyedView = views[2*i+1];
			if(keyedView.equals(view)) {
				navig.getNavigationBar("e1")
					.addItem(
						new TextNavigationItem(key)
					);
			} else {
				navig.getNavigationBar("e1")
				.addItem(
					new ActionNavigationItem(key, "AdminSetDetailedChoiceConf")
					.addParameter("assessmentID", d.getAssessment().getId())
					.addParameter("selectedView", keyedView)
				);
			}
		}
	}

}
