package hr.fer.zemris.jcms.web.navig.builders.course.applications;

import hr.fer.zemris.jcms.service.has.HasCourseInstance;

import hr.fer.zemris.jcms.web.actions.data.ApplicationAdminAproveData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.ActionNavigationItem;
import hr.fer.zemris.jcms.web.navig.Navigation;
import hr.fer.zemris.jcms.web.navig.NavigationBuilder;
import hr.fer.zemris.jcms.web.navig.TextNavigationItem;
import hr.fer.zemris.jcms.web.navig.builders.course.CourseBuilderPart;

public class ApplicationAdminAproveBuilder extends NavigationBuilder {

	public static void build(Navigation navig, AbstractActionData actionData, boolean root) {
		HasCourseInstance d = (HasCourseInstance)actionData;
		navig.suggestPageTitle(actionData.getMessageLogger().getText("Navigation.courseHome") + " " +d.getCourseInstance().getCourse().getName());
		CourseBuilderPart.build(navig, actionData, false);
		navig.getNavigationBar("m2")
			.addItem(
				new ActionNavigationItem(d.getCourseInstance().getCourse().getName(), false, "ShowCourse")
				.addParameter("courseInstanceID", d.getCourseInstance().getId())
			);
		navig.getNavigationBar("m2")
			.addItem(
				new ActionNavigationItem("Navigation.applicationsList", true, "ApplicationMain")
				.addParameter("courseInstanceID", d.getCourseInstance().getId())
			);
		ApplicationAdminAproveData aData = (ApplicationAdminAproveData)actionData;
		if(aData.getFromDefinitionID()==null) {
			navig.getNavigationBar("m2")
				.addItem(
					new ActionNavigationItem("Navigation.applicationTable", true, "ApplicationAdminTable")
					.addParameter("courseInstanceID", d.getCourseInstance().getId())
				);
		} else {
			navig.getNavigationBar("m2")
				.addItem(
					new ActionNavigationItem("Navigation.applicationListStudents", true, "ApplicationListStudents")
					.addParameter("courseInstanceID", d.getCourseInstance().getId())
					.addParameter("definitionID", aData.getFromDefinitionID())
				);
		}
		navig.getNavigationBar("m2")
			.addItem(
				new TextNavigationItem("Navigation.applicationApproveForStudent")
			);

	}

}
