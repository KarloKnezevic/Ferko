package hr.fer.zemris.jcms.web.actions2.course.assessments;

import hr.fer.zemris.jcms.service2.course.assessments.AssessmentsBrowsingService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.AdminAssessmentViewData;

@WebClass(dataClass=AdminAssessmentViewData.class)
public class AdminAssessmentView extends Ext2ActionSupport<AdminAssessmentViewData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo
    public String execute() throws Exception {
		AssessmentsBrowsingService.adminAssessmentView(getEntityManager(), data);
        return null;
    }

    public String getAssessmentID() {
		return data.getAssessmentID();
	}
    public void setAssessmentID(String assessmentID) {
		data.setAssessmentID(assessmentID);
	}
}
