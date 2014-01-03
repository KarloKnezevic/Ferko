package hr.fer.zemris.jcms.web.navig.builders.course.wiki;

import hr.fer.zemris.jcms.service.has.HasCourseInstance;
import hr.fer.zemris.jcms.service2.course.wiki.CourseWikiUtil;
import hr.fer.zemris.jcms.web.actions.data.CourseWikiData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.ActionNavigationItem;
import hr.fer.zemris.jcms.web.navig.Navigation;
import hr.fer.zemris.jcms.web.navig.NavigationBuilder;
import hr.fer.zemris.jcms.web.navig.TextNavigationItem;
import hr.fer.zemris.jcms.web.navig.builders.course.CourseBuilderPart;

public class CourseWikiBuilder extends NavigationBuilder {

	public static void build(Navigation navig, AbstractActionData actionData, boolean root) {
		HasCourseInstance d = (HasCourseInstance)actionData;
		navig.suggestPageTitle(actionData.getMessageLogger().getText("Navigation.courseHome") + " " +d.getCourseInstance().getCourse().getName());
		CourseBuilderPart.build(navig, actionData, false);
		navig.getNavigationBar("m2")
			.addItem(
				new ActionNavigationItem(d.getCourseInstance().getCourse().getName(), false, "ShowCourse")
				.addParameter("courseInstanceID", d.getCourseInstance().getId())
			);
		if(actionData instanceof CourseWikiData) {
			CourseWikiData cwd = (CourseWikiData)actionData;
			if(root && !cwd.getPageComponents().isEmpty()) root = false;
			if(root) {
				navig.getNavigationBar("m2")
					.addItem(
						new TextNavigationItem("Navigation.courseWiki")
				);
			} else {
				navig.getNavigationBar("m2")
					.addItem(
						new ActionNavigationItem("Navigation.courseWiki", true, "CourseWiki")
						.addParameter("courseInstanceID", d.getCourseInstance().getId())
				);
				if(!cwd.isNavigationDisabled()) {
					for(int i = 0; i < cwd.getPageComponents().size()-1; i++) {
						String[] arr = new String[i+1];
						for(int j = 0; j <= i; j++) {
							arr[j] = cwd.getPageComponents().get(j);
						}
						navig.getNavigationBar("m2")
							.addItem(
								new ActionNavigationItem(arr[i], false, "CourseWiki")
								.addParameter("pageURL", CourseWikiUtil.buildPageURL(arr))
								.addParameter("courseInstanceID", d.getCourseInstance().getId())
							);
					}
				}
				if(!cwd.getPageComponents().isEmpty()) {
					navig.getNavigationBar("m2")
						.addItem(
							new TextNavigationItem(cwd.getPageComponents().get(cwd.getPageComponents().size()-1), false)
					);
				}
			}
		} else {
			if(root) {
				navig.getNavigationBar("m2")
					.addItem(
						new TextNavigationItem("Navigation.courseWiki")
				);
			} else {
				navig.getNavigationBar("m2")
					.addItem(
						new ActionNavigationItem("Navigation.courseWiki", true, "CourseWiki")
						.addParameter("courseInstanceID", d.getCourseInstance().getId())
				);
			}
		}
	}

}
