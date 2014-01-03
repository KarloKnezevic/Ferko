package hr.fer.zemris.jcms.beans;

import java.util.ArrayList;
import java.util.List;

public class TestDataBean {
	private List<TestInstanceDataBean> testInstanceData = new ArrayList<TestInstanceDataBean>();
	private String overallStatus;
	private double testScore;
	private int totalAttempts;
	private String sourceSystem;
	private boolean valid;
	private String invalidReason;
	
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	public String getInvalidReason() {
		return invalidReason;
	}
	public void setInvalidReason(String invalidReason) {
		this.invalidReason = invalidReason;
	}
	public String getSourceSystem() {
		return sourceSystem;
	}
	public void setSourceSystem(String sourceSystem) {
		this.sourceSystem = sourceSystem;
	}
	public List<TestInstanceDataBean> getTestInstanceData() {
		return testInstanceData;
	}
	public void setTestInstanceData(List<TestInstanceDataBean> testInstanceData) {
		this.testInstanceData = testInstanceData;
	}
	public String getOverallStatus() {
		return overallStatus;
	}
	public void setOverallStatus(String overallStatus) {
		this.overallStatus = overallStatus;
	}
	public double getTestScore() {
		return testScore;
	}
	public void setTestScore(double testScore) {
		this.testScore = testScore;
	}
	public int getTotalAttempts() {
		return totalAttempts;
	}
	public void setTotalAttempts(int totalAttempts) {
		this.totalAttempts = totalAttempts;
	} 
}
