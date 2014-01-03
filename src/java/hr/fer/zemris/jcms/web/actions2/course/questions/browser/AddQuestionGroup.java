package hr.fer.zemris.jcms.web.actions2.course.questions.browser;

import hr.fer.zemris.jcms.service2.course.questions.browser.QuestionBrowserService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.AddQuestionGroupData;
import hr.fer.zemris.jcms.web.navig.builders.course.questions.browser.QuestionBrowserBuilder;

@WebClass(dataClass = AddQuestionGroupData.class, defaultNavigBuilder = QuestionBrowserBuilder.class, defaultNavigBuilderIsRoot = false, additionalMenuItems = {"m2", "Navigation.questionGroup"})
public class AddQuestionGroup extends Ext2ActionSupport<AddQuestionGroupData> {
	
	private static final long serialVersionUID = 1L;

	@WebMethodInfo
	public String execute() {
		QuestionBrowserService.prepareNewGroup(getEntityManager(), data);
		return null;
	}
	
	@WebMethodInfo
	public String edit() {
		QuestionBrowserService.prepareGroupEdit(getEntityManager(), data);
		return null;
	}
	
	@WebMethodInfo
	public String save() {
		QuestionBrowserService.saveOrUpdateGroup(getEntityManager(), data);
		return null;
	}
	
	public String getCourseInstanceID() {
		return data.getCourseInstanceID();
	}

	public void setCourseInstanceID(String courseInstanceID) {
		data.setCourseInstanceID(courseInstanceID);
	}
	
	public Long getQuestionGroupID() {
		return data.getQuestionGroupID();
	}
	
	public void setQuestionGroupID(Long questionGroupID) {
		data.setQuestionGroupID(questionGroupID);
	}
	
}
