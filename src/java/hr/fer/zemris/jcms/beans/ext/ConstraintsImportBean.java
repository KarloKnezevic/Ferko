package hr.fer.zemris.jcms.beans.ext;

public class ConstraintsImportBean {
	private String isvuCode;
	private int type;
	private int count;
	private String constraint;
	
	public ConstraintsImportBean() {
	}
	
	public ConstraintsImportBean(String isvuCode, int type, int count,
			String constraint) {
		super();
		this.isvuCode = isvuCode;
		this.type = type;
		this.count = count;
		this.constraint = constraint;
	}

	public String getIsvuCode() {
		return isvuCode;
	}

	public void setIsvuCode(String isvuCode) {
		this.isvuCode = isvuCode;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getConstraint() {
		return constraint;
	}

	public void setConstraint(String constraint) {
		this.constraint = constraint;
	}
}
