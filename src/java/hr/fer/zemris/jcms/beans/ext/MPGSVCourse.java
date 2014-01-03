package hr.fer.zemris.jcms.beans.ext;

import hr.fer.zemris.util.StringUtil;

import java.util.List;

public class MPGSVCourse implements Comparable<MPGSVCourse> {
	private String isvuCode;
	private String courseName;
	private MPGSVMarketPlace marketPlace;
	private List<MPGSVGroup> groups;

	public MPGSVCourse() {
	}

	public String getIsvuCode() {
		return isvuCode;
	}
	public void setIsvuCode(String isvuCode) {
		this.isvuCode = isvuCode;
	}

	public String getCourseName() {
		return courseName;
	}
	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public MPGSVMarketPlace getMarketPlace() {
		return marketPlace;
	}
	public void setMarketPlace(MPGSVMarketPlace marketPlace) {
		this.marketPlace = marketPlace;
	}

	public List<MPGSVGroup> getGroups() {
		return groups;
	}
	public void setGroups(List<MPGSVGroup> groups) {
		this.groups = groups;
	}

	public String getFullCourseName() {
		return courseName + "(" + isvuCode + ")";
	}
	
	@Override
	public int compareTo(MPGSVCourse o) {
		int r = StringUtil.HR_COLLATOR.compare(this.getCourseName(), o.getCourseName());
		if(r!=0) return r;
		return this.getIsvuCode().compareTo(o.getIsvuCode());
	}
}
