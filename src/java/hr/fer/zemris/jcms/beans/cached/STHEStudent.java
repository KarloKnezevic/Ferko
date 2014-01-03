package hr.fer.zemris.jcms.beans.cached;

public class STHEStudent extends ScoreTableHeaderEntry {

	private static final long serialVersionUID = 1L;

	public STHEStudent(Long id) {
		super(id);
	}

	@Override
	public String getSortKey() {
		return "S:N:-1";
	}

	@Override
	public String getReverseSortKey() {
		return "S:R:-1";
	}
	
	@Override
	public String getUniqueID() {
		return id+":S";
	}
}
