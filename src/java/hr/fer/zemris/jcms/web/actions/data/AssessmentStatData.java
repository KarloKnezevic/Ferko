package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.statistics.assessments.AssessmentStatistics;

import hr.fer.zemris.jcms.statistics.assessments.StatisticsBase;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.DeleteOnCloseFileInputStream;

/**
 * Podatkovna struktura za akciju {@link AdminAssessmentList}.
 *  
 * @author marcupic
 *
 */
public class AssessmentStatData extends BaseAssessment {
	
	private StatisticsBase statBase;
	private AssessmentStatistics stat;
	
	private String assessmentID;
	private Integer localID;
	private Integer bins;
	DeleteOnCloseFileInputStream stream;

	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public AssessmentStatData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
	
	public StatisticsBase getStatBase() {
		return statBase;
	}
	public void setStatBase(StatisticsBase statBase) {
		this.statBase = statBase;
	}

	public void setStat(AssessmentStatistics stat) {
		this.stat = stat;
	}
	public AssessmentStatistics getStat() {
		return stat;
	}
	
    public Integer getLocalID() {
		return localID;
	}
    public void setLocalID(Integer localID) {
		this.localID = localID;
	}
    
    public String getAssessmentID() {
		return assessmentID;
	}
    public void setAssessmentID(String assessmentID) {
		this.assessmentID = assessmentID;
	}
    
    public Integer getBins() {
		return bins;
	}
    public void setBins(Integer bins) {
		this.bins = bins;
	}
    
    public DeleteOnCloseFileInputStream getStream() {
		return stream;
	}
    public void setStream(DeleteOnCloseFileInputStream stream) {
		this.stream = stream;
	}

}
