package hr.fer.zemris.jcms.beans.cached;

import java.io.Serializable;

public abstract class ScoreTableEntry implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long id;

	public ScoreTableEntry(Long id) {
		super();
		this.id = id;
	}
	
	public Long getId() {
		return id;
	}
	
	public abstract byte getType();
}
