package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.beans.cached.CourseScoreTable;
import hr.fer.zemris.jcms.beans.cached.ScoreTableEntry;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.List;
import java.util.Set;

/**
 * Podatkovna struktura za akciju {@link AdminAssessmentSummaryView}.
 *  
 * @author marcupic
 *
 */
public class AdminAssessmentSummaryViewData extends BaseCourseInstance {
	
	private CourseScoreTable table;
	private Set<Long> allowedUsers;
	private List<ScoreTableEntry[]> entries;
	private String headersJSON;
	private String dataJSON;
	private String dependenciesJSON;
	private Group selectedGroup;
	
	private String courseInstanceID;
	private String sortKey = "S:N:-1";
	private Long selectedGroupID;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public AdminAssessmentSummaryViewData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

    public String getCourseInstanceID() {
		return courseInstanceID;
	}
    public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
    
    public String getSortKey() {
		return sortKey;
	}
    public void setSortKey(String sortKey) {
		this.sortKey = sortKey;
	}

    public Long getSelectedGroupID() {
		return selectedGroupID;
	}
    public void setSelectedGroupID(Long selectedGroupID) {
		this.selectedGroupID = selectedGroupID;
	}

	public Set<Long> getAllowedUsers() {
		return allowedUsers;
	}
	public void setAllowedUsers(Set<Long> allowedUsers) {
		this.allowedUsers = allowedUsers;
	}
	
	public CourseScoreTable getTable() {
		return table;
	}
	public void setTable(CourseScoreTable table) {
		this.table = table;
	}

	public List<ScoreTableEntry[]> getEntries() {
		return entries;
	}
	public void setEntries(List<ScoreTableEntry[]> entries) {
		this.entries = entries;
	}
	
	public String getHeadersJSON() {
		return headersJSON;
	}
	public void setHeadersJSON(String headersJSON) {
		this.headersJSON = headersJSON;
	}
	
	public String getDataJSON() {
		return dataJSON;
	}
	public void setDataJSON(String dataJSON) {
		this.dataJSON = dataJSON;
	}
	
	public String getDependenciesJSON() {
		return dependenciesJSON;
	}
	public void setDependenciesJSON(String dependenciesJSON) {
		this.dependenciesJSON = dependenciesJSON;
	}

	public Group getSelectedGroup() {
		return selectedGroup;
	}
	public void setSelectedGroup(Group selectedGroup) {
		this.selectedGroup = selectedGroup;
	}
}
