package hr.fer.zemris.jcms.beans.cached;

public class STHEFlag extends ScoreTableHeaderEntry {

	private static final long serialVersionUID = 1L;
	private String shortName;
	
	public STHEFlag(Long id, String shortName) {
		super(id);
		this.shortName = shortName;
	}

	public String getShortName() {
		return shortName;
	}

	@Override
	public String getSortKey() {
		return "F:N:"+getId();
	}

	@Override
	public String getReverseSortKey() {
		return "F:R:"+getId();
	}
	
	@Override
	public String toString() {
		return shortName;
	}
	
	@Override
	public String getUniqueID() {
		return shortName+":F";
	}
}
