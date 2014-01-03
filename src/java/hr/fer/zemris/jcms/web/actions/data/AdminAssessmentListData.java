package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.model.Assessment;

import hr.fer.zemris.jcms.model.AssessmentFlag;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.Collections;
import java.util.List;

/**
 * Podatkovna struktura za akciju {@link AdminAssessmentList}.
 *  
 * @author marcupic
 *
 */
public class AdminAssessmentListData extends BaseCourseInstance {
	
	private String courseInstanceID;
	private List<Assessment> assessments;
	private List<AssessmentFlag> assessmentFlags;
	private boolean canManageAssessments;
	private List<Group> groupsToDisplay;
	private Group selectedGroup;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public AdminAssessmentListData(IMessageLogger messageLogger) {
		super(messageLogger);
		List<Assessment> l = Collections.emptyList();
		List<AssessmentFlag> l2 = Collections.emptyList();
		setAssessments(l);
		setAssessmentFlags(l2);
	}

    public String getCourseInstanceID() {
		return courseInstanceID;
	}
    
    public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

	public List<AssessmentFlag> getAssessmentFlags() {
		return assessmentFlags;
	}
	public void setAssessmentFlags(List<AssessmentFlag> assessmentFlags) {
		this.assessmentFlags = assessmentFlags;
	}
	
	public List<Assessment> getAssessments() {
		return assessments;
	}
	public void setAssessments(List<Assessment> assessments) {
		this.assessments = assessments;
	}

	public void setCanManageAssessments(boolean canManageAssessments) {
		this.canManageAssessments = canManageAssessments;
	}
	public boolean getCanManageAssessments() {
		return canManageAssessments;
	}

	public List<Group> getGroupsToDisplay() {
		return groupsToDisplay;
	}
	public void setGroupsToDisplay(List<Group> groupsToDisplay) {
		this.groupsToDisplay = groupsToDisplay;
	}

	public Group getSelectedGroup() {
		return selectedGroup;
	}
	public void setSelectedGroup(Group selectedGroup) {
		this.selectedGroup = selectedGroup;
	}
}
