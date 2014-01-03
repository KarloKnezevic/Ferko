package hr.fer.zemris.jcms.beans;

public class CourseBean {
	
	private String name;
	private String isvuCode;
	
	public CourseBean(String isvuCode, String name) {
		super();
		this.isvuCode = isvuCode;
		this.name = name;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIsvuCode() {
		return isvuCode;
	}
	public void setIsvuCode(String isvuCode) {
		this.isvuCode = isvuCode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((isvuCode == null) ? 0 : isvuCode.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CourseBean))
			return false;
		final CourseBean other = (CourseBean) obj;
		if (isvuCode == null) {
			if (other.isvuCode != null)
				return false;
		} else if (!isvuCode.equals(other.isvuCode))
			return false;
		return true;
	}
}
