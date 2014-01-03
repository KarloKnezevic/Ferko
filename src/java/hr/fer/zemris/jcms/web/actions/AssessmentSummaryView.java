package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.AssessmentSummaryViewData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

/**
 * Ovu metodu više ne koristiti. Zamjena je u {@link hr.fer.zemris.jcms.web.actions2.course.assessments.AssessmentSummaryView}.
 * @author marcupic
 *
 */
@Deprecated
public class AssessmentSummaryView extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private String courseInstanceID;
	private AssessmentSummaryViewData data = null;
	
    public String execute() throws Exception {
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new AssessmentSummaryViewData(MessageLoggerFactory.createMessageLogger(this));
    	if(getCourseInstanceID()==null || getCourseInstanceID().equals("") || !hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
    	if(hasCurrentUser()) {
    		BasicBrowsing.getAssessmentSummaryViewData(data, getCurrentUser().getUserID(), getCourseInstanceID());
    	}
        return SUCCESS;
    }

    public String getCourseInstanceID() {
		return courseInstanceID;
	}
    public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
    
    public AssessmentSummaryViewData getData() {
		return data;
	}
    public void setData(AssessmentSummaryViewData data) {
		this.data = data;
	}
}
