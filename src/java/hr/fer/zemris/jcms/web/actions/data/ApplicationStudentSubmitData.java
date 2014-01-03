package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.beans.StudentApplicationBean;
import hr.fer.zemris.jcms.model.ApplicationDefinition;
import hr.fer.zemris.jcms.model.StudentApplication;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

public class ApplicationStudentSubmitData extends BaseCourseInstance {

	private String courseInstanceID;
	private String applicationID;
	private StudentApplicationBean bean;

	private ApplicationDefinition definition;
	private StudentApplication application;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public ApplicationStudentSubmitData(IMessageLogger messageLogger) {
		super(messageLogger);
		bean = new StudentApplicationBean();
	}
	
	/**
	 * Provjera znanja koja je upravo uređena/stvorena. Dok akcija nije uspješna
	 * (za slučajstvaranja provjere), ovo će biti null; kod uređivanja, bit će
	 * postavljeno ako je moguće.
	 * 
	 * @return provjeru
	 */
	public StudentApplication getApplication() {
		return application;
	}
	public void setApplication(StudentApplication application) {
		this.application = application;
	}

	public ApplicationDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(ApplicationDefinition definition) {
		this.definition = definition;
	}
	
	public String getApplicationID() {
		return applicationID;
	}
	
	public void setApplicationID(String applicationID) {
		this.applicationID = applicationID;
	}
	
	public String getCourseInstanceID() {
		return courseInstanceID;
	}
	
	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
	
	public StudentApplicationBean getBean() {
		return bean;
	}
	public void setBean(StudentApplicationBean bean) {
		this.bean = bean;
	}
}
