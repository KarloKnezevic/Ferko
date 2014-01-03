package hr.fer.zemris.jcms.beans.ext;

public class ExchangeDescriptor {

	private String fromGroupTag;
	private String fromGroup;
	private String fromStudentTag;
	private String toGroupTag;
	private String toGroup;
	private String toStudentTag;
	
	public ExchangeDescriptor() {
	}
	
	public ExchangeDescriptor(String fromGroup, String fromGroupTag, String fromStudentTag,
			String toGroup, String toGroupTag, String toStudentTag) {
		super();
		this.fromGroup = fromGroup;
		this.fromGroupTag = fromGroupTag;
		this.fromStudentTag = fromStudentTag;
		this.toGroup = toGroup;
		this.toGroupTag = toGroupTag;
		this.toStudentTag = toStudentTag;
	}

	public String getFromGroupTag() {
		return fromGroupTag;
	}

	public void setFromGroupTag(String fromGroupTag) {
		this.fromGroupTag = fromGroupTag;
	}

	public String getFromStudentTag() {
		return fromStudentTag;
	}

	public void setFromStudentTag(String fromStudentTag) {
		this.fromStudentTag = fromStudentTag;
	}

	public String getToGroupTag() {
		return toGroupTag;
	}

	public void setToGroupTag(String toGroupTag) {
		this.toGroupTag = toGroupTag;
	}

	public String getToStudentTag() {
		return toStudentTag;
	}

	public void setToStudentTag(String toStudentTag) {
		this.toStudentTag = toStudentTag;
	}

	public String getFromGroup() {
		return fromGroup;
	}

	public void setFromGroup(String fromGroup) {
		this.fromGroup = fromGroup;
	}

	public String getToGroup() {
		return toGroup;
	}

	public void setToGroup(String toGroup) {
		this.toGroup = toGroup;
	}

	@Override
	public String toString() {
		return fromGroup+"("+fromGroupTag+")"+"/"+fromStudentTag+":"+toGroup+"("+toGroupTag+")"+"/"+toStudentTag;
	}
}
