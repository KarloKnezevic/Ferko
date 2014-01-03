package hr.fer.zemris.jcms.web.actions.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hr.fer.zemris.jcms.beans.GradingPolicyBean;
import hr.fer.zemris.jcms.model.Grade;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.jcms.web.actions2.course.grades.ShowGradingPolicy;
import hr.fer.zemris.util.DeleteOnCloseFileInputStream;

/**
 * Podatkovna struktura za akciju {@link ShowGradingPolicy}.
 *  
 * @author marcupic
 *
 */
public class GradingPolicyData extends BaseCourseInstance {

	private String courseInstanceID;
	private boolean editable;
	private Map<String, Object> rules = new HashMap<String, Object>();
	private GradingPolicyBean bean = new GradingPolicyBean();
	private List<Grade> allGrades;
	private DeleteOnCloseFileInputStream stream;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public GradingPolicyData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public DeleteOnCloseFileInputStream getStream() {
		return stream;
	}
	public void setStream(DeleteOnCloseFileInputStream stream) {
		this.stream = stream;
	}
	
	public String getCourseInstanceID() {
		return courseInstanceID;
	}

	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
	
	public boolean getEditable() {
		return editable;
	}
	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	
	public Map<String, Object> getRules() {
		return rules;
	}
	public void setRules(Map<String, Object> rules) {
		this.rules = rules;
	}
	
	public GradingPolicyBean getBean() {
		return bean;
	}
	public void setBean(GradingPolicyBean bean) {
		this.bean = bean;
	}

	public List<Grade> getAllGrades() {
		return allGrades;
	}
	public void setGrades(List<Grade> allGrades) {
		this.allGrades = allGrades;
	}
}
