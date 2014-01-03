package hr.fer.zemris.jcms.web.actions2.course.assessments;

import hr.fer.zemris.jcms.beans.AssessmentFlagBean;
import hr.fer.zemris.jcms.service2.course.assessments.AssessmentsEditingService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.AdminAssessmentFlagEditData;

/**
 * Akcija za stvaranje, editiranje i ažuriranje zastavica.
 * 
 * @author marcupic
 */
@WebClass(dataClass=AdminAssessmentFlagEditData.class)
public class AdminAssessmentFlagEdit extends Ext2ActionSupport<AdminAssessmentFlagEditData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo
    public String newFlag() throws Exception {
		AssessmentsEditingService.adminAssessmentFlagNew(getEntityManager(), data);
		return null;
    }

	@WebMethodInfo
    public String editFlag() throws Exception {
		AssessmentsEditingService.adminAssessmentFlagEdit(getEntityManager(), data);
		return null;
    }

	@WebMethodInfo
    public String saveFlag() throws Exception {
		AssessmentsEditingService.adminAssessmentFlagSaveOrUpdate(getEntityManager(), data);
		return null;
    }

	@WebMethodInfo
    public String execute() throws Exception {
    	return newFlag();
    }

    public AssessmentFlagBean getBean() {
		return data.getBean();
	}
    public void setBean(AssessmentFlagBean bean) {
		data.setBean(bean);
	}
}
