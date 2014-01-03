package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.jcms.web.actions2.ActivityGo;

/**
 * Podatkovna struktura za akciju {@link ActivityGo}.
 *  
 * @author marcupic
 *
 */
public class ActivityGoData extends AbstractActionData {

	private Long aid;
	private String parentID;
	private String courseInstanceID;
	private Long studentApplicationID;
	private Long itemID;
	private Long issueID;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public ActivityGoData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
	
	public Long getAid() {
		return aid;
	}
	public void setAid(Long aid) {
		this.aid = aid;
	}
	
	public String getCourseInstanceID() {
		return courseInstanceID;
	}
	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

	public Long getStudentApplicationID() {
		return studentApplicationID;
	}
	public void setStudentApplicationID(Long studentApplicationID) {
		this.studentApplicationID = studentApplicationID;
	}
	public String getParentID() {
		return parentID;
	}
	public void setParentID(String parentID) {
		this.parentID = parentID;
	}
	public Long getItemID() {
		return itemID;
	}
	public void setItemID(Long itemID) {
		this.itemID = itemID;
	}

	public Long getIssueID() {
		return issueID;
	}

	public void setIssueID(Long issueID) {
		this.issueID = issueID;
	}
	
	
}
