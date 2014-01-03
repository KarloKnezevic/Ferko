package hr.fer.zemris.jcms.beans;

import java.util.List;

public class CCTAMatrix {
	private List<CCTAMatrixRow> rows;
	private String[] taskNames;

	public CCTAMatrix(String[] taskNames, List<CCTAMatrixRow> rows) {
		super();
		this.taskNames = taskNames;
		this.rows = rows;
	}

	public String[] getTaskNames() {
		return taskNames;
	}
	
	public List<CCTAMatrixRow> getRows() {
		return rows;
	}
}
