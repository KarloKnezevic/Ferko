package hr.fer.zemris.jcms.beans.ext;

import java.io.Serializable;

public class MPSecurityConstraint implements Serializable {

	private static final long serialVersionUID = 2L;

	private String fromGroupTag;
	private String toGroupTag;
	private String studentTag;

	public MPSecurityConstraint(String studentTag, String fromGroupTag, String toGroupTag) {
		super();
		this.fromGroupTag = fromGroupTag;
		this.studentTag = studentTag;
		this.toGroupTag = toGroupTag;
	}
	
	public String getFromGroupTag() {
		return fromGroupTag;
	}
	public String getToGroupTag() {
		return toGroupTag;
	}
	public String getStudentTag() {
		return studentTag;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fromGroupTag == null) ? 0 : fromGroupTag.hashCode());
		result = prime * result
				+ ((studentTag == null) ? 0 : studentTag.hashCode());
		result = prime * result
				+ ((toGroupTag == null) ? 0 : toGroupTag.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MPSecurityConstraint other = (MPSecurityConstraint) obj;
		if (fromGroupTag == null) {
			if (other.fromGroupTag != null)
				return false;
		} else if (!fromGroupTag.equals(other.fromGroupTag))
			return false;
		if (studentTag == null) {
			if (other.studentTag != null)
				return false;
		} else if (!studentTag.equals(other.studentTag))
			return false;
		if (toGroupTag == null) {
			if (other.toGroupTag != null)
				return false;
		} else if (!toGroupTag.equals(other.toGroupTag))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return studentTag+":"+fromGroupTag+"/"+toGroupTag;
	}
}
