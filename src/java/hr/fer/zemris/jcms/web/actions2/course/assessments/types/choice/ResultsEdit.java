package hr.fer.zemris.jcms.web.actions2.course.assessments.types.choice;

import hr.fer.zemris.jcms.beans.ext.ConfChoiceScoreEditBean;
import hr.fer.zemris.jcms.service2.course.assessments.types.choice.ChoiceResultsEditingService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.ConfChoiceScoreEditData;
import hr.fer.zemris.jcms.web.navig.builders.course.assessments.AdminAssessmentViewBuilder;

@WebClass(dataClass=ConfChoiceScoreEditData.class, defaultNavigBuilder=AdminAssessmentViewBuilder.class,
		defaultNavigBuilderIsRoot=false, additionalMenuItems={"m2","AssessmentFlags.nav.resultsEditing"})
public class ResultsEdit extends Ext2ActionSupport<ConfChoiceScoreEditData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo
    public String edit() throws Exception {
		ChoiceResultsEditingService.fetchStudentResults(getEntityManager(), data);
		return null;
    }

	@WebMethodInfo
    public String save() throws Exception {
		ChoiceResultsEditingService.updateStudentResults(getEntityManager(), data);
		return null;
    }

	@WebMethodInfo
    public String pickLetter() throws Exception {
		ChoiceResultsEditingService.fetchStudentResults(getEntityManager(), data);
		return null;
    }

	@WebMethodInfo
    public String execute() throws Exception {
    	return edit();
    }

	public String getAssessmentID() {
		return data.getBean().getAssessmentID();
	}
	public void setAssessmentID(String assessmentID) {
		data.getBean().setAssessmentID(assessmentID);
	}

    public ConfChoiceScoreEditBean getBean() {
		return data.getBean();
	}
    public void setBean(ConfChoiceScoreEditBean bean) {
		data.setBean(bean);
	}
    
    
}
