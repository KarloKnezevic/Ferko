package hr.fer.zemris.jcms.web.navig.builders.course.grades;

import hr.fer.zemris.jcms.service.has.HasCourseInstance;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.ActionNavigationItem;
import hr.fer.zemris.jcms.web.navig.Navigation;
import hr.fer.zemris.jcms.web.navig.NavigationBuilder;
import hr.fer.zemris.jcms.web.navig.TextNavigationItem;

public class ShowGradesInternalBuilder extends NavigationBuilder {

	public static void build(Navigation navig, AbstractActionData actionData, boolean root) {
		HasCourseInstance d = (HasCourseInstance)actionData;
		navig.suggestPageTitle(actionData.getMessageLogger().getText("Navigation.courseHome") + " " +d.getCourseInstance().getCourse().getName());
		// Ako želim povratak na kolegij, onda ide ovo zakomentirano;
		// kod je tu tako da se kasnije može dodati zastavica koja kaže kuda se točno moram moći vratiti 
		// CourseBuilderPart.build(navig, actionData, false);
		ShowGradingPolicyBuilder.build(navig, actionData, false);
		
		if(root) {
			navig.getNavigationBar("m2")
				.addItem(
					new TextNavigationItem("Navigation.grades")
			);
		} else {
			navig.getNavigationBar("m2")
				.addItem(
					new ActionNavigationItem("Navigation.grades", true, "ShowGradingPolicy", "showGrades")
					.addParameter("courseInstanceID", d.getCourseInstance().getId())
			);
		}
	
		// Ako želim povratak na kolegij, onda ide ovo zakomentirano;
		// kod je tu tako da se kasnije može dodati zastavica koja kaže kuda se točno moram moći vratiti 
//		navig.getNavigationBar("m2")
//			.addItem(
//				new ActionNavigationItem(d.getCourseInstance().getCourse().getName(), false, "ShowCourse")
//				.addParameter("courseInstanceID", d.getCourseInstance().getId())
//			);
//		if(root) {
//			navig.getNavigationBar("m2")
//				.addItem(
//					new TextNavigationItem("Navigation.gradingPolicy")
//			);
//		} else {
//			navig.getNavigationBar("m2")
//				.addItem(
//					new ActionNavigationItem("Navigation.gradingPolicy", true, "ShowGradingPolicy")
//					.addParameter("courseInstanceID", d.getCourseInstance().getId())
//			);
//		}
	}

}
