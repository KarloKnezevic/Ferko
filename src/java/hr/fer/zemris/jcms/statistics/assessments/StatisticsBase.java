package hr.fer.zemris.jcms.statistics.assessments;

import java.io.Serializable;

public class StatisticsBase implements Serializable {

	private static final long serialVersionUID = 1L;
	private int id;
	private String kind;
	
	public StatisticsBase() {
	}

	public String getKind() {
		return kind;
	}
	public void setKind(String kind) {
		this.kind = kind;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getStatisticsBaseType() {
		return -1;
	}
}
