package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.beans.StudentApplicationBean;
import hr.fer.zemris.jcms.model.StudentApplication;
import hr.fer.zemris.jcms.model.extra.ApplicationStatus;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class ApplicationStudentViewData extends BaseCourseInstance {
		
	private static final long serialVersionUID = 1L;

	private String courseInstanceID;
	private Long applicationID;
	private StudentApplication application;
	private Map<ApplicationStatus, String> statuses = null;
	private SimpleDateFormat sdf;
	private StudentApplicationBean bean;
	
		/**
		 * Konstruktor.
		 * @param messageLogger lokalizirane poruke
		 */
	
	public ApplicationStudentViewData(IMessageLogger messageLogger) {
		super(messageLogger);
		statuses = new HashMap<ApplicationStatus, String>();
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for(ApplicationStatus as : ApplicationStatus.values()){
			statuses.put(as, messageLogger.getText(as.name()));
		}
	}

	public StudentApplication getApplication() {
		return application;
	}

	public void setApplication(StudentApplication application) {
		this.application = application;
	}

	public Map<ApplicationStatus, String> getStatuses() {
		return statuses;
	}

	public void setStatuses(Map<ApplicationStatus, String> statuses) {
		this.statuses = statuses;
	}

	public SimpleDateFormat getSdf() {
		return sdf;
	}

	public void setSdf(SimpleDateFormat sdf) {
		this.sdf = sdf;
	}
	
    public String getCourseInstanceID() {
		return courseInstanceID;
	}
    public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

    public Long getApplicationID() {
		return applicationID;
	}
    public void setApplicationID(Long applicationID) {
		this.applicationID = applicationID;
	}

    public StudentApplicationBean getBean() {
    	if(bean==null) {
    		bean = new StudentApplicationBean();
    	}
		return bean;
	}
    public void setBean(StudentApplicationBean bean) {
		this.bean = bean;
	}
}
