package hr.fer.zemris.jcms.beans.cached;

public class STHEAssessment extends ScoreTableHeaderEntry {

	private static final long serialVersionUID = 1L;
	private String shortName;
	
	public STHEAssessment(Long id, String shortName) {
		super(id);
		this.shortName = shortName;
	}

	public String getShortName() {
		return shortName;
	}
	
	@Override
	public String getSortKey() {
		return "A:N:"+getId();
	}

	@Override
	public String getReverseSortKey() {
		return "A:R:"+getId();
	}
	
	@Override
	public String toString() {
		return shortName;
	}
	
	@Override
	public String getUniqueID() {
		return shortName+":A";
	}
}
