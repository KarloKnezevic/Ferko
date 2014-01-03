package hr.fer.zemris.jcms.beans.ext;

public class MPOfferBean {
	private String courseInstanceID;
	private Long parentID;
	private Long myUserGroupID;
	private Long myGroupID;
	private Long groupID;
	private boolean requireApr;
	private String validUntil;
	private String reason;
	private Long offerID;
	private String toUsername;
	
	public String getCourseInstanceID() {
		return courseInstanceID;
	}
	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
	public Long getParentID() {
		return parentID;
	}
	public void setParentID(Long parentID) {
		this.parentID = parentID;
	}
	public Long getMyUserGroupID() {
		return myUserGroupID;
	}
	public void setMyUserGroupID(Long myUserGroupID) {
		this.myUserGroupID = myUserGroupID;
	}
	public Long getMyGroupID() {
		return myGroupID;
	}
	public void setMyGroupID(Long myGroupID) {
		this.myGroupID = myGroupID;
	}
	public Long getGroupID() {
		return groupID;
	}
	public void setGroupID(Long groupID) {
		this.groupID = groupID;
	}
	public boolean isRequireApr() {
		return requireApr;
	}
	public void setRequireApr(boolean requireApr) {
		this.requireApr = requireApr;
	}
	public String getValidUntil() {
		return validUntil;
	}
	public void setValidUntil(String validUntil) {
		this.validUntil = validUntil;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public Long getOfferID() {
		return offerID;
	}
	public void setOfferID(Long offerID) {
		this.offerID = offerID;
	}
	public String getToUsername() {
		return toUsername;
	}
	public void setToUsername(String toUsername) {
		this.toUsername = toUsername;
	}
}
