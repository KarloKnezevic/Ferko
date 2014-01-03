package hr.fer.zemris.jcms.beans;

import java.util.List;

public class CCIAMatrix {
	private List<CCIAMatrixRow> rows;
	private String[] assessmentNames;

	public CCIAMatrix(String[] assessmentNames, List<CCIAMatrixRow> rows) {
		super();
		this.assessmentNames = assessmentNames;
		this.rows = rows;
	}

	public String[] getAssessmentNames() {
		return assessmentNames;
	}
	
	public List<CCIAMatrixRow> getRows() {
		return rows;
	}
}
