package hr.fer.zemris.jcms.statistics.assessments;

import java.io.Serializable;

public class ScoreStatistics extends StatisticsBase implements Serializable {

	private static final long serialVersionUID = 1L;
	private double average;
	private double median;
	private int count;
	private double minimum;
	private double maximum;
	private double[] allScore;
	
	public ScoreStatistics() {
	}

	public double getAverage() {
		return average;
	}

	public void setAverage(double average) {
		this.average = average;
	}

	public double getMedian() {
		return median;
	}

	public void setMedian(double median) {
		this.median = median;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public double[] getAllScore() {
		return allScore;
	}

	public void setAllScore(double[] allScore) {
		this.allScore = allScore;
	}
	
	@Override
	public int getStatisticsBaseType() {
		return 1;
	}
	
	public double getMinimum() {
		return minimum;
	}
	public void setMinimum(double minimum) {
		this.minimum = minimum;
	}
	
	public double getMaximum() {
		return maximum;
	}
	public void setMaximum(double maximum) {
		this.maximum = maximum;
	}
}
