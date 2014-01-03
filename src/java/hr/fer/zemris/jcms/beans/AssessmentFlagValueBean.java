package hr.fer.zemris.jcms.beans;

public class AssessmentFlagValueBean {

	private Long id;
	private String studentFirstName;
	private String studentLastName;
	private String studentJMBAG;
	private Long studentId;
	private String assignerFirstName;
	private String assignerLastName;
	private String assignerJMBAG;
	private Long assignerId;
	private boolean value;
	private boolean manuallySet;
	private boolean manualValue;
	private boolean originalManuallySet;
	private boolean originalManualValue;
	private boolean error;
	private long version;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getStudentFirstName() {
		return studentFirstName;
	}
	public void setStudentFirstName(String studentFirstName) {
		this.studentFirstName = studentFirstName;
	}
	public String getStudentLastName() {
		return studentLastName;
	}
	public void setStudentLastName(String studentLastName) {
		this.studentLastName = studentLastName;
	}
	public String getStudentJMBAG() {
		return studentJMBAG;
	}
	public void setStudentJMBAG(String studentJMBAG) {
		this.studentJMBAG = studentJMBAG;
	}
	public Long getStudentId() {
		return studentId;
	}
	public void setStudentId(Long studentId) {
		this.studentId = studentId;
	}
	public String getAssignerFirstName() {
		return assignerFirstName;
	}
	public void setAssignerFirstName(String assignerFirstName) {
		this.assignerFirstName = assignerFirstName;
	}
	public String getAssignerLastName() {
		return assignerLastName;
	}
	public void setAssignerLastName(String assignerLastName) {
		this.assignerLastName = assignerLastName;
	}
	public String getAssignerJMBAG() {
		return assignerJMBAG;
	}
	public void setAssignerJMBAG(String assignerJMBAG) {
		this.assignerJMBAG = assignerJMBAG;
	}
	public Long getAssignerId() {
		return assignerId;
	}
	public void setAssignerId(Long assignerId) {
		this.assignerId = assignerId;
	}
	public boolean isValue() {
		return value;
	}
	public void setValue(boolean value) {
		this.value = value;
	}
	public boolean isManuallySet() {
		return manuallySet;
	}
	public void setManuallySet(boolean manuallySet) {
		this.manuallySet = manuallySet;
	}
	public boolean isManualValue() {
		return manualValue;
	}
	public void setManualValue(boolean manualValue) {
		this.manualValue = manualValue;
	}
	public boolean isOriginalManualValue() {
		return originalManualValue;
	}
	public void setOriginalManualValue(boolean originalManualValue) {
		this.originalManualValue = originalManualValue;
	}
	public boolean isOriginalManuallySet() {
		return originalManuallySet;
	}
	public void setOriginalManuallySet(boolean originalManuallySet) {
		this.originalManuallySet = originalManuallySet;
	}
	public boolean isError() {
		return error;
	}
	public void setError(boolean error) {
		this.error = error;
	}
	public long getVersion() {
		return version;
	}
	public void setVersion(long version) {
		this.version = version;
	}

}
