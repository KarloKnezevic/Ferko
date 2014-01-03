package hr.fer.zemris.jcms.beans.ext;

import java.util.Date;

public class MPGSVMarketPlace {
	private Long id;
	private boolean open;
	private Date openFrom;
	private Date openUntil;
	private String formulaConstraints;
	private String securityConstraints;
	private int timeBuffer;
	private boolean absent;
	
	public MPGSVMarketPlace() {
	}
	
	public boolean isAbsent() {
		return absent;
	}
	public void setAbsent(boolean absent) {
		this.absent = absent;
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
	
	public Date getOpenFrom() {
		return openFrom;
	}
	public void setOpenFrom(Date openFrom) {
		this.openFrom = openFrom;
	}
	
	public Date getOpenUntil() {
		return openUntil;
	}
	public void setOpenUntil(Date openUntil) {
		this.openUntil = openUntil;
	}
	
	public String getFormulaConstraints() {
		return formulaConstraints;
	}
	public void setFormulaConstraints(String formulaConstraints) {
		this.formulaConstraints = formulaConstraints;
	}
	
	public String getSecurityConstraints() {
		return securityConstraints;
	}
	public void setSecurityConstraints(String securityConstraints) {
		this.securityConstraints = securityConstraints;
	}
	
	public int getTimeBuffer() {
		return timeBuffer;
	}
	public void setTimeBuffer(int timeBuffer) {
		this.timeBuffer = timeBuffer;
	}
}
