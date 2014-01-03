package hr.fer.zemris.jcms.beans.cached;

import java.io.Serializable;

public abstract class ScoreTableHeaderEntry implements Serializable {
	
	private static final long serialVersionUID = 1L;
	protected Long id;
	
	public ScoreTableHeaderEntry(Long id) {
		super();
		this.id = id;
	}

	public Long getId() {
		return id;
	}
	
	public abstract String getSortKey();

	public abstract String getReverseSortKey();

	public abstract String getUniqueID();
}
