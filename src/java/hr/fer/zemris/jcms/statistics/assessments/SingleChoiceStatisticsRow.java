package hr.fer.zemris.jcms.statistics.assessments;

import hr.fer.zemris.util.NumberUtil;

import java.io.Serializable;

public class SingleChoiceStatisticsRow implements Serializable {

	private static final long serialVersionUID = 1L;

	private String key;
	private int totalStudents;
	private int correctStudents;
	private int wrongStudents;
	private int unansweredStudents;
	private double weightAbsolute;
	private double weightRelative;
	private double discriminationIndex;
	private boolean coarse;
	
	public SingleChoiceStatisticsRow(String key, int totalStudents,
			int correctStudents, int unansweredStudents, int wrongStudents,
			double discriminationIndex, double weightAbsolute,
			double weightRelative, boolean coarse) {
		super();
		this.key = key;
		this.totalStudents = totalStudents;
		this.correctStudents = correctStudents;
		this.unansweredStudents = unansweredStudents;
		this.wrongStudents = wrongStudents;
		this.discriminationIndex = discriminationIndex;
		this.weightAbsolute = weightAbsolute;
		this.weightRelative = weightRelative;
		this.coarse = coarse;
	}

	public String getKey() {
		return key;
	}

	public int getTotalStudents() {
		return totalStudents;
	}

	public int getCorrectStudents() {
		return correctStudents;
	}

	public int getWrongStudents() {
		return wrongStudents;
	}

	public int getUnansweredStudents() {
		return unansweredStudents;
	}

	public double getWeightAbsolute() {
		return weightAbsolute;
	}

	public double getWeightRelative() {
		return weightRelative;
	}

	public double getDiscriminationIndex() {
		return discriminationIndex;
	}
	
	public String getWeightAbsoluteAsString() {
		return NumberUtil.simpleDoubleToString(weightAbsolute, 3);
	}

	public String getWeightRelativeAsString() {
		return NumberUtil.simpleDoubleToString(weightRelative, 3);
	}

	public String getDiscriminationIndexAsString() {
		return NumberUtil.simpleDoubleToString(discriminationIndex, 3);
	}
	
	public boolean isCoarse() {
		return coarse;
	}
}
