package hr.fer.zemris.jcms.web.actions2.course.assessments;

import hr.fer.zemris.jcms.service2.course.assessments.AssessmentsRecalcService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.AdminAssessmentRecalcData;
import hr.fer.zemris.jcms.web.navig.builders.BuilderDefault;

@WebClass(dataClass=AdminAssessmentRecalcData.class, defaultNavigBuilder=BuilderDefault.class)
public class AssessmentRecalc extends Ext2ActionSupport<AdminAssessmentRecalcData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo(lockPath="ml\\ci${courseInstanceID}\\a")
    public String execute() throws Exception {
		AssessmentsRecalcService.recalculateAssessments(getEntityManager(), data);
        return null;
    }

	public String getCourseInstanceID() {
		return data.getCourseInstanceID();
	}
	public void setCourseInstanceID(String courseInstanceID) {
		data.setCourseInstanceID(courseInstanceID);
	}

}
