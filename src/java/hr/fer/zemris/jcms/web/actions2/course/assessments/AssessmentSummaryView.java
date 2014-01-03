package hr.fer.zemris.jcms.web.actions2.course.assessments;

import hr.fer.zemris.jcms.service2.course.assessments.AssessmentsStudentViewService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.AssessmentSummaryViewData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;

@WebClass(dataClass=AssessmentSummaryViewData.class)
public class AssessmentSummaryView extends Ext2ActionSupport<AssessmentSummaryViewData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo(
		dataResultMappings={@DataResultMapping(dataResult="popup",struts2Result="popup",registerDelayedMessages=false)}
	)
    public String execute() throws Exception {
    	AssessmentsStudentViewService.prepareStudentSummaryView(getEntityManager(), data);
    	if(data.isImposter() && AbstractActionData.RESULT_SUCCESS.equals(data.getResult())) {
    		data.setResult("popup");
    	}
        return null;
    }

    public String getCourseInstanceID() {
		return data.getCourseInstanceID();
	}
    public void setCourseInstanceID(String courseInstanceID) {
		data.setCourseInstanceID(courseInstanceID);
	}
}
