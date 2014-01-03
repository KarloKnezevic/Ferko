package hr.fer.zemris.jcms.web.actions2.course.assessments;

import hr.fer.zemris.jcms.beans.AssessmentBean;
import hr.fer.zemris.jcms.service2.course.assessments.AssessmentsEditingService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.AdminAssessmentEditData;

/**
 * Akcija za stvaranje, editiranje i ažuriranje provjera.
 * 
 * @author marcupic
 */
@WebClass(dataClass=AdminAssessmentEditData.class)
public class AdminAssessmentEdit extends Ext2ActionSupport<AdminAssessmentEditData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo
    public String newAssessment() throws Exception {
		AssessmentsEditingService.adminAssessmentNew(getEntityManager(), data);
		return null;
    }

	@WebMethodInfo
    public String editAssessment() throws Exception {
		AssessmentsEditingService.adminAssessmentEdit(getEntityManager(), data);
		return null;
    }

	@WebMethodInfo
    public String saveAssessment() throws Exception {
		AssessmentsEditingService.adminAssessmentSaveOrUpdate(getEntityManager(), data);
		return null;
    }

	@WebMethodInfo
    public String execute() throws Exception {
    	return newAssessment();
    }

    public AssessmentBean getBean() {
		return data.getBean();
	}
    public void setBean(AssessmentBean bean) {
		data.setBean(bean);
	}
}
