package hr.fer.zemris.jcms.beans.cached;

import hr.fer.zemris.jcms.model.AssessmentFlagValue;

public class STEFlagValue extends ScoreTableEntry {

	private static final long serialVersionUID = 1L;
	
	private boolean value;
	private boolean manuallySet;
	private boolean manualValue;
	private boolean error;
	
	public STEFlagValue(Long id, boolean value, boolean manualValue, boolean manuallySet, boolean error) {
		super(id);
		this.error = error;
		this.manualValue = manualValue;
		this.manuallySet = manuallySet;
		this.value = value;
	}

	public STEFlagValue(AssessmentFlagValue as) {
		super(as.getId());
		this.error = as.getError();
		this.manualValue = as.getManualValue();
		this.manuallySet = as.getManuallySet();
		this.value = as.getValue();
	}

	public boolean isValue() {
		return value;
	}

	public boolean isManuallySet() {
		return manuallySet;
	}

	public boolean isManualValue() {
		return manualValue;
	}

	public boolean isError() {
		return error;
	}
	
	@Override
	public String toString() {
		if(error) return "*";
		if(value) return "1";
		return "0";
	}
	
	@Override
	public byte getType() {
		return (byte)2;
	}
}
