package hr.fer.zemris.jcms.beans.ext;

import hr.fer.zemris.jcms.beans.GroupBean;

import java.util.ArrayList;
import java.util.List;

public class MarketPlaceBean {
	private List<GroupBean> groups = new ArrayList<GroupBean>();
	private Long id;
	private boolean open;
	private String openFrom;
	private String openUntil;
	private String formulaConstraints;
	private String securityConstraints;
	private int timeBuffer;

	public List<GroupBean> getGroups() {
		return groups;
	}
	public void setGroups(List<GroupBean> groups) {
		this.groups = groups;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public boolean isOpen() {
		return open;
	}
	public void setOpen(boolean open) {
		this.open = open;
	}
	public String getOpenFrom() {
		return openFrom;
	}
	public void setOpenFrom(String openFrom) {
		this.openFrom = openFrom;
	}
	public String getOpenUntil() {
		return openUntil;
	}
	public void setOpenUntil(String openUntil) {
		this.openUntil = openUntil;
	}
	public String getFormulaConstraints() {
		return formulaConstraints;
	}
	public void setFormulaConstraints(String formulaConstraints) {
		if(formulaConstraints!=null && formulaConstraints.trim().length()==0) {
			this.formulaConstraints = null;
		} else {
			this.formulaConstraints = formulaConstraints;
		}
	}
	public String getSecurityConstraints() {
		return securityConstraints;
	}
	public void setSecurityConstraints(String securityConstraints) {
		if(securityConstraints!=null && securityConstraints.trim().length()==0) {
			this.securityConstraints = null;
		} else {
			this.securityConstraints = securityConstraints;
		}
	}
	public int getTimeBuffer() {
		return timeBuffer;
	}
	public void setTimeBuffer(int timeBuffer) {
		this.timeBuffer = timeBuffer;
	}
}
