package hr.fer.zemris.jcms.beans.ext;

import hr.fer.zemris.jcms.beans.TestDataBean;

public class ComponentItemAssessmentBean {
	private String id;
	private String assessmentIdentifier;
	private TestDataBean testDataBean;
	
	public ComponentItemAssessmentBean() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAssessmentIdentifier() {
		return assessmentIdentifier;
	}

	public void setAssessmentIdentifier(String assessmentIdentifier) {
		this.assessmentIdentifier = assessmentIdentifier;
	}
	
	public TestDataBean getTestDataBean() {
		return testDataBean;
	}
	
	public void setTestDataBean(TestDataBean testDataBean) {
		this.testDataBean = testDataBean;
	}
}
