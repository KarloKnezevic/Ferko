package hr.fer.zemris.jcms.web.actions2.course.assessments;

import hr.fer.zemris.jcms.service2.course.assessments.AssessmentStatService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.TransactionalMethod;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.AssessmentStatData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.builders.course.assessments.AdminAssessmentListBuilder;

@WebClass(dataClass=AssessmentStatData.class, defaultNavigBuilder=AdminAssessmentListBuilder.class,
		defaultNavigBuilderIsRoot=false,additionalMenuItems={"m2","Assessments.statistics"})
public class AssessmentStat extends Ext2ActionSupport<AssessmentStatData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo
    public String execute() throws Exception {
		AssessmentStatService.getStatistics(getEntityManager(), data);
        return null;
    }

	@WebMethodInfo(
		dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="stream",registerDelayedMessages=true)},
		struts2ResultMappings={@Struts2ResultMapping(struts2Result="stream",transactionalMethod=@TransactionalMethod(closeImmediately=true))}
	)
    public String scoreHistogram() throws Exception {
    	AssessmentStatService.getScoreHistogram(getEntityManager(), data);
        return null;
    }

    public Integer getLocalID() {
		return data.getLocalID();
	}
    public void setLocalID(Integer localID) {
		data.setLocalID(localID);
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

}
