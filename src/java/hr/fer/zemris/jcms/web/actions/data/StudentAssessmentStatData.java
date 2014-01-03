package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.statistics.assessments.AssessmentStatistics;

import hr.fer.zemris.jcms.statistics.assessments.StatisticsBase;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.jcms.web.actions2.course.assessments.StudentAssessmentStat;
import hr.fer.zemris.util.DeleteOnCloseFileInputStream;

/**
 * Podatkovna struktura za akciju {@link StudentAssessmentStat}.
 *  
 * @author marcupic
 *
 */
public class StudentAssessmentStatData extends BaseAssessment {
	
	private StatisticsBase statBase;
	private AssessmentStatistics stat;
	private String kind;
	
	private String assessmentID;
	private Integer bins;
	DeleteOnCloseFileInputStream stream;

	private boolean imposter;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public StudentAssessmentStatData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
	
	public boolean isImposter() {
		return imposter;
	}
	public void setImposter(boolean imposter) {
		this.imposter = imposter;
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

    /**
     * Koju vrstu statistike želimo prikazati studentu? Mogućnosti su:
     * <ul>
     * <li>"E" - efektivnu</li>
     * <li>"A" - od same provjere, bez nasljeđenih bodova</li>
     * </ul>
     * Napomena: student uvijek vidi samo i isključivo globalnu statistiku
     * za čitav kolegij; nema pristupa statistici po grupama.
     * 
     * @return vrsta statistike
     */
    public String getKind() {
		return kind;
	}
    public void setKind(String kind) {
		this.kind = kind;
	}
}
