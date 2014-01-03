package hr.fer.zemris.jcms.beans.ext;

public class AssessmentViewBean {
	protected String confType;
	
	protected AssessmentViewConfDataBean data;
	
	public AssessmentViewBean() {
	}

	public String getConfType() {
		return confType;
	}

	public void setConfType(String confType) {
		this.confType = confType;
	}

	public AssessmentViewConfDataBean getData() {
		return data;
	}

	public void setData(AssessmentViewConfDataBean data) {
		this.data = data;
	}
}
