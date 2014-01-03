package hr.fer.zemris.jcms.beans;

/**
 * Razred koji modelira trajanje (u minutama) provjere koja je flagirana
 * navedenom zastavicom.
 * 
 * @author marcupic
 *
 */
public class CIP1TermDuration {
	
	private long assessmentTagID;
	private String caption;
	private int duration;

	public CIP1TermDuration() {
		// TODO Auto-generated constructor stub
	}
	
	public CIP1TermDuration(long assessmentTagID, String caption, int duration) {
		super();
		this.assessmentTagID = assessmentTagID;
		this.caption = caption;
		this.duration = duration;
	}

	public long getAssessmentTagID() {
		return assessmentTagID;
	}
	public String getCaption() {
		return caption;
	}
	public int getDuration() {
		return duration;
	}
	public void setAssessmentTagID(long assessmentTagID) {
		this.assessmentTagID = assessmentTagID;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
}
