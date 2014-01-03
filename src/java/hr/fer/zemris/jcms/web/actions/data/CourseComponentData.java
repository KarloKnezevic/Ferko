package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.beans.CCIAMatrix;

import hr.fer.zemris.jcms.beans.CCTAMatrix;
import hr.fer.zemris.jcms.beans.CourseComponentBean;
import hr.fer.zemris.jcms.beans.CourseComponentTaskBean;
import hr.fer.zemris.jcms.beans.GroupBean;
import hr.fer.zemris.jcms.beans.ext.ComponentDefBean;
import hr.fer.zemris.jcms.beans.ext.ComponentItemAssessmentBean;
import hr.fer.zemris.jcms.beans.ext.ComponentTaskAssignmentBean;
import hr.fer.zemris.jcms.beans.ext.ComponentUserTaskBean;
import hr.fer.zemris.jcms.beans.ext.FileBean;
import hr.fer.zemris.jcms.beans.ext.FileDownloadBean;
import hr.fer.zemris.jcms.model.CourseComponent;
import hr.fer.zemris.jcms.model.CourseComponentItem;
import hr.fer.zemris.jcms.model.CourseComponentTask;
import hr.fer.zemris.jcms.model.CourseComponentTaskAssignment;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.GroupOwner;
import hr.fer.zemris.jcms.model.GroupWideEvent;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CourseComponentData extends BaseCourseInstance{

	private String filterGroupID;

	private Set<CourseComponent> componentSet = Collections.emptySet();
	private List<CourseComponentBean> descriptorList = Collections.emptyList();
	private List<CourseComponentTaskBean> taskList = Collections.emptyList();
	private List<ComponentItemAssessmentBean> itemAssessmentsList = Collections.emptyList();
	private List<FileBean> itemFiles = Collections.emptyList();
	private List<ComponentDefBean> defList = Collections.emptyList();
	private List<GroupBean> groupList = Collections.emptyList();
	private List<User> userList = Collections.emptyList();
	private List<String> letters = Collections.emptyList();
	private Group group;
	private CourseComponent courseComponent;
	private CourseComponentItem courseComponentItem;
	private CourseComponentTask courseComponentTask;
	private CourseComponentTaskAssignment courseComponentTaskAssignment;
	private ComponentUserTaskBean userTask;
	private ComponentItemAssessmentBean itemAssessmentBean;
	private ComponentTaskAssignmentBean assignmentBean;
	private CourseComponentTaskBean taskBean;
	private FileDownloadBean downloadBean;
	private boolean admin;
	private boolean staffMember;
	private boolean locked;
	private boolean ok;
	private String jmbag;
	private CCIAMatrix cciaMatrix;
	private CCTAMatrix cctaMatrix;
	private GroupOwner owner;
	private String userSelection;

	private List<TermAssisstantData> termAssistants;

	public String getFilterGroupID() {
		return filterGroupID;
	}
	public void setFilterGroupID(String filterGroupID) {
		this.filterGroupID = filterGroupID;
	}
	
	public CourseComponentData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public List<TermAssisstantData> getTermAssistants() {
		return termAssistants;
	}
	
	public void setTermAssistants(List<TermAssisstantData> termAssistants) {
		this.termAssistants = termAssistants;
	}
	
	public GroupOwner getOwner() {
		return owner;
	}
	public void setOwner(GroupOwner owner) {
		this.owner = owner;
	}
	
	public Set<CourseComponent> getComponentSet() {
		return componentSet;
	}

	public void setComponentSet(Set<CourseComponent> componentSet) {
		this.componentSet = componentSet;
	}

	public List<CourseComponentBean> getDescriptorList() {
		return descriptorList;
	}

	public void setDescriptorList(List<CourseComponentBean> descriptorList) {
		this.descriptorList = descriptorList;
	}

	public CourseComponent getCourseComponent() {
		return courseComponent;
	}

	public void setCourseComponent(CourseComponent courseComponent) {
		this.courseComponent = courseComponent;
	}

	public CourseComponentItem getCourseComponentItem() {
		return courseComponentItem;
	}

	public void setCourseComponentItem(CourseComponentItem courseComponentItem) {
		this.courseComponentItem = courseComponentItem;
	}


	public ComponentUserTaskBean getUserTask() {
		return userTask;
	}


	public void setUserTask(ComponentUserTaskBean userTask) {
		this.userTask = userTask;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}


	public List<CourseComponentTaskBean> getTaskList() {
		return taskList;
	}


	public void setTaskList(List<CourseComponentTaskBean> taskList) {
		this.taskList = taskList;
	}


	public CourseComponentTask getCourseComponentTask() {
		return courseComponentTask;
	}


	public void setCourseComponentTask(CourseComponentTask courseComponentTask) {
		this.courseComponentTask = courseComponentTask;
	}


	public ComponentItemAssessmentBean getItemAssessmentBean() {
		return itemAssessmentBean;
	}


	public void setItemAssessmentBean(ComponentItemAssessmentBean itemAssessmentBean) {
		this.itemAssessmentBean = itemAssessmentBean;
	}


	public boolean isLocked() {
		return locked;
	}


	public void setLocked(boolean locked) {
		this.locked = locked;
	}


	public List<ComponentItemAssessmentBean> getItemAssessmentsList() {
		return itemAssessmentsList;
	}


	public void setItemAssessmentsList(
			List<ComponentItemAssessmentBean> itemAssessmentsList) {
		this.itemAssessmentsList = itemAssessmentsList;
	}


	public List<FileBean> getItemFiles() {
		return itemFiles;
	}


	public void setItemFiles(List<FileBean> itemFiles) {
		this.itemFiles = itemFiles;
	}


	public List<ComponentDefBean> getDefList() {
		return defList;
	}

	public void setDefList(List<ComponentDefBean> defList) {
		this.defList = defList;
	}


	public List<String> getLetters() {
		return letters;
	}


	public void setLetters(List<String> letters) {
		this.letters = letters;
	}


	public boolean isStaffMember() {
		return staffMember;
	}


	public void setStaffMember(boolean staffMember) {
		this.staffMember = staffMember;
	}


	public ComponentTaskAssignmentBean getAssignmentBean() {
		return assignmentBean;
	}


	public void setAssignmentBean(ComponentTaskAssignmentBean assignmentBean) {
		this.assignmentBean = assignmentBean;
	}


	public CourseComponentTaskAssignment getCourseComponentTaskAssignment() {
		return courseComponentTaskAssignment;
	}


	public void setCourseComponentTaskAssignment(
			CourseComponentTaskAssignment courseComponentTaskAssignment) {
		this.courseComponentTaskAssignment = courseComponentTaskAssignment;
	}


	public CourseComponentTaskBean getTaskBean() {
		return taskBean;
	}


	public void setTaskBean(CourseComponentTaskBean taskBean) {
		this.taskBean = taskBean;
	}


	public List<GroupBean> getGroupList() {
		return groupList;
	}


	public void setGroupList(List<GroupBean> groupList) {
		this.groupList = groupList;
	}


	public Group getGroup() {
		return group;
	}


	public void setGroup(Group group) {
		this.group = group;
	}


	public List<User> getUserList() {
		return userList;
	}


	public void setUserList(List<User> userList) {
		this.userList = userList;
	}


	public FileDownloadBean getDownloadBean() {
		return downloadBean;
	}


	public void setDownloadBean(FileDownloadBean downloadBean) {
		this.downloadBean = downloadBean;
	}


	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}


	public String getJmbag() {
		return jmbag;
	}


	public void setJmbag(String jmbag) {
		this.jmbag = jmbag;
	}
	
	public CCIAMatrix getCciaMatrix() {
		return cciaMatrix;
	}
	
	public void setCciaMatrix(CCIAMatrix cciaMatrix) {
		this.cciaMatrix = cciaMatrix;
	}
	
	public CCTAMatrix getCctaMatrix() {
		return cctaMatrix;
	}
	
	public void setCctaMatrix(CCTAMatrix cctaMatrix) {
		this.cctaMatrix = cctaMatrix;
	}
	
	public String getUserSelection() {
		return userSelection;
	}
	public void setUserSelection(String userSelection) {
		this.userSelection = userSelection;
	}
	
	/**
	 * Pomocni razred koji cuva popis asistenata na terminu vjezbe.
	 * Koristimo kod prikaza tih informacija studentima.
	 * 
	 * @author marcupic
	 */
	public static class TermAssisstantData {
		private Group group;
		private List<GroupWideEvent> events;
		private List<User> assistants;
		
		public TermAssisstantData(Group group, List<GroupWideEvent> events, List<User> assistants) {
			super();
			this.group = group;
			this.events = events;
			this.assistants = assistants;
		}

		public List<GroupWideEvent> getEvents() {
			return events;
		}
		
		public Group getGroup() {
			return group;
		}
		
		public List<User> getAssistants() {
			return assistants;
		}
	}
}
