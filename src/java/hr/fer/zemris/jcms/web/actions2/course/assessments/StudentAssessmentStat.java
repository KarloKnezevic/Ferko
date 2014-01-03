package hr.fer.zemris.jcms.web.actions2.course.assessments;

import hr.fer.zemris.jcms.service2.course.assessments.AssessmentStatService;

import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.TransactionalMethod;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.StudentAssessmentStatData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.builders.course.assessments.AssessmentViewBuilder;

@WebClass(dataClass=StudentAssessmentStatData.class, defaultNavigBuilder=AssessmentViewBuilder.class,
		defaultNavigBuilderIsRoot=false,additionalMenuItems={"m2","Assessments.statistics"})
public class StudentAssessmentStat extends Ext2ActionSupport<StudentAssessmentStatData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo(
		dataResultMappings={@DataResultMapping(dataResult="popup",struts2Result="popup",registerDelayedMessages=false)}
	)
    public String execute() throws Exception {
		AssessmentStatService.getStudentStatistics(getEntityManager(), data);
    	if(data.isImposter() && AbstractActionData.RESULT_SUCCESS.equals(data.getResult())) {
    		data.setResult("popup");
    	}
        return null;
    }

	@WebMethodInfo(
		dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="stream",registerDelayedMessages=true)},
		struts2ResultMappings={@Struts2ResultMapping(struts2Result="stream",transactionalMethod=@TransactionalMethod(closeImmediately=true))}
	)
    public String scoreHistogram() throws Exception {
    	AssessmentStatService.getStudentScoreHistogram(getEntityManager(), data);
        return null;
    }

    public String getAssessmentID() {
		return data.getAssessmentID();
	}
    public void setAssessmentID(String assessmentID) {
		data.setAssessmentID(assessmentID);
	}
    
    public Integer getBins() {
		return data.getBins();
	}
    public void setBins(Integer bins) {
		data.setBins(bins);
	}
    
    public String getKind() {
		return data.getKind();
	}
    public void setKind(String kind) {
		data.setKind(kind);
	}
}
