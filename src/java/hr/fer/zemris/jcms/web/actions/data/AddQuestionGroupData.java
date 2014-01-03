package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.beans.QuestionGroupBean;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

/**
 * Podatkovna struktura za akciju {@link ShowCourse}.
 *  
 * @author marcupic
 *
 */
public class AddQuestionGroupData extends BaseCourseInstance {

	private String courseInstanceID;
	private Long questionGroupID;
	private QuestionGroupBean bean = new QuestionGroupBean();
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public AddQuestionGroupData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public String getCourseInstanceID() {
		return courseInstanceID;
	}

	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
	
	public QuestionGroupBean getBean() {
		return bean;
	}
	
	public void setBean(QuestionGroupBean bean) {
		this.bean = bean;
	}
	
	public Long getQuestionGroupID() {
		return questionGroupID;
	}
	
	public void setQuestionGroupID(Long questionGroupID) {
		this.questionGroupID = questionGroupID;
	}
}
