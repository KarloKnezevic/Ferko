package hr.fer.zemris.jcms.beans;

import java.util.ArrayList;
import java.util.List;

public class GradingPolicyBean {
	private Long id;
	private String gradesVisibility;
	private List<StringNameStringValue> gradesVisibilities;
	private boolean gradesValid;
	private boolean gradesLocked;
	private String policyImplementation;
	private List<StringNameStringValue> policyImplementations;
	private String termDate;
	private List<GroupGraderBean> graders = new ArrayList<GroupGraderBean>();
	private List<StringNameStringValue> graderUsers = new ArrayList<StringNameStringValue>();

	public List<StringNameStringValue> getGraderUsers() {
		return graderUsers;
	}
	public void setGraderUsers(List<StringNameStringValue> graderUsers) {
		this.graderUsers = graderUsers;
	}
	public List<GroupGraderBean> getGraders() {
		return graders;
	}
	public void setGraders(List<GroupGraderBean> graders) {
		this.graders = graders;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getGradesVisibility() {
		return gradesVisibility;
	}
	public void setGradesVisibility(String gradesVisibility) {
		this.gradesVisibility = gradesVisibility;
	}
	public List<StringNameStringValue> getGradesVisibilities() {
		return gradesVisibilities;
	}
	public void setGradesVisibilities(List<StringNameStringValue> gradesVisibilities) {
		this.gradesVisibilities = gradesVisibilities;
	}
	public boolean getGradesValid() {
		return gradesValid;
	}
	public void setGradesValid(boolean gradesValid) {
		this.gradesValid = gradesValid;
	}
	public boolean getGradesLocked() {
		return gradesLocked;
	}
	public void setGradesLocked(boolean gradesLocked) {
		this.gradesLocked = gradesLocked;
	}
	public String getPolicyImplementation() {
		return policyImplementation;
	}
	public void setPolicyImplementation(String policyImplementation) {
		this.policyImplementation = policyImplementation;
	}
	public String getTermDate() {
		return termDate;
	}
	public void setTermDate(String termDate) {
		this.termDate = termDate;
	}
	public List<StringNameStringValue> getPolicyImplementations() {
		return policyImplementations;
	}
	public void setPolicyImplementations(
			List<StringNameStringValue> policyImplementations) {
		this.policyImplementations = policyImplementations;
	}
}
