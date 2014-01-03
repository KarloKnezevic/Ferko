package hr.fer.zemris.jcms.web.actions.data;
 
import hr.fer.zemris.jcms.beans.ext.ToDoBean;
import hr.fer.zemris.jcms.beans.ext.ToDoCourseGroupsBean;
import hr.fer.zemris.jcms.beans.ext.ToDoRealizerBean;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.extra.ToDoTaskPriority;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.jcms.web.interceptors.data.CurrentUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Podatkovna struktura za dohvat podataka o ToDo listama
 *  
 * @author IvanFer
 *
 */
public class ToDoData extends BaseAssessment {
	
	/* Lista vlastitih todo taskova */
	private List<ToDoBean> ownList;
	/* Lista zadanih todo taskova */
	private List<ToDoBean> assignedList;
	private ToDoTaskPriority priorities;
	/* Lista grupa dostupnih trenutnom korisniku */
	private List<ToDoCourseGroupsBean> userGroups;
	/* Za validaciju kod dodavanja nove grupe realizatora */
	private Group groupForValidation;
	/* Selektirana grupa kod dodavanja novog grupnog realizatora */
	private String selectedGroupID = null;
	private Map<Long, Group> userGroupsMap;
	/* Indikator radi li se o studentu ili nestudentu */
	private boolean renderBothToDoLists = true;
	/* Lista dostupnih predložaka */
	private List<ToDoBean> templateList;
	private Map<Long, ToDoBean> templateMap;
	/* User varijabla za potrebe dodavanja novog individualnog korisnika */
	private User singleUser;
	/* Uvijek trenutni korisnik */
	private CurrentUser currUsr;
	/* Lista realizatora */
	private List<ToDoRealizerBean> realizers;
	private ToDoBean loadedTask;
	private Long taskToLoadID;
	private Long taskToInstantiateID;
	/* Podzadaci aktualnog zadatka*/
	private List<ToDoBean> subTasks;
	/* Indikator o uklanjanju realizatora */
	private boolean userRemoval = false;
	/* Indikator da se kreira novi predložak */
	private Boolean newTemplate;
	private ToDoBean newSubTask;
	private ToDoBean newTask;
	/* Postavljen na ID taska koji se editira (indicira editiranje postojećeg taska) */
	private String taskId;
	/* Postavljen na ID podtaska koji se editira (indicira editiranje postojećeg podtaska) */
	private String subTaskID=null;
	
	/* Zastavica je li korisnik administrator - nudi se dohvat svih grupa */
	private boolean isAdmin = false;
	private boolean allGroups = false;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public ToDoData(IMessageLogger messageLogger) {
		super(messageLogger);
		this.userGroups = new ArrayList<ToDoCourseGroupsBean>();
		this.subTasks = new ArrayList<ToDoBean>();
		this.newSubTask = new ToDoBean();
		this.newTask = new ToDoBean();
		this.realizers = new ArrayList<ToDoRealizerBean>();
	}

	public List<ToDoBean> getOwnList() {
		return ownList;
	}

	public void setOwnList(List<ToDoBean> ownList) {
		this.ownList = ownList;
	}

	public List<ToDoBean> getAssignedList() {
		return assignedList;
	}

	public void setAssignedList(List<ToDoBean> assignedList) {
		this.assignedList = assignedList;
	}

	@SuppressWarnings("static-access")
	public List<ToDoTaskPriority> getPriorities() {
		return Arrays.asList(priorities.values());
	}

	public void setPriorities(ToDoTaskPriority priorities) {
		this.priorities = priorities;
	}


	public List<ToDoCourseGroupsBean> getUserGroups() {
		return userGroups;
	}

	public void setUserGroups(List<ToDoCourseGroupsBean> userGroups) {
		this.userGroups = userGroups;
	}

	public boolean isRenderBothToDoLists() {
		return renderBothToDoLists;
	}
	
	public boolean getRenderBothToDoLists() {
		return renderBothToDoLists;
	}

	public void setRenderBothToDoLists(boolean renderBothToDoLists) {
		this.renderBothToDoLists = renderBothToDoLists;
	}

	public List<ToDoBean> getTemplateList() {
		return templateList;
	}

	public void setTemplateList(List<ToDoBean> templateList) {
		this.templateList = templateList;
	}

	public Map<Long, ToDoBean> getTemplateMap() {
		return templateMap;
	}

	public void setTemplateMap(Map<Long, ToDoBean> templateMap) {
		this.templateMap = templateMap;
	}

	public User getSingleUser() {
		return singleUser;
	}

	public void setSingleUser(User singleUser) {
		this.singleUser = singleUser;
	}

	public Map<Long, Group> getUserGroupsMap() {
		return userGroupsMap;
	}

	public void setUserGroupsMap(Map<Long, Group> userGroupsMap) {
		this.userGroupsMap = userGroupsMap;
	}

	public Long getTaskToLoadID() {
		return taskToLoadID;
	}

	public void setTaskToLoadID(Long taskToLoadID) {
		this.taskToLoadID = taskToLoadID;
	}

	public void setLoadedTask(ToDoBean loadedTask) {
		this.loadedTask = loadedTask;
	}

	public ToDoBean getLoadedTask() {
		return loadedTask;
	}

	public Group getGroupForValidation() {
		return groupForValidation;
	}

	public void setGroupForValidation(Group groupForValidation) {
		this.groupForValidation = groupForValidation;
	}

	public Long getTaskToInstantiateID() {
		return taskToInstantiateID;
	}

	public void setTaskToInstantiateID(Long taskToInstantiateID) {
		this.taskToInstantiateID = taskToInstantiateID;
	}

	public boolean isUserRemoval() {
		return userRemoval;
	}

	public void setUserRemoval(boolean userRemoval) {
		this.userRemoval = userRemoval;
	}

	public String getSelectedGroupID() {
		return selectedGroupID;
	}

	public void setSelectedGroupID(String selectedGroupID) {
		this.selectedGroupID = selectedGroupID;
	}

	public Boolean getNewTemplate() {
		return newTemplate;
	}

	public void setNewTemplate(Boolean newTemplate) {
		this.newTemplate = newTemplate;
	}

	public List<ToDoBean> getSubTasks() {
		return subTasks;
	}
	public void setSubTasks(List<ToDoBean> subTasks) {
		this.subTasks = subTasks;
	}

	public CurrentUser getCurrUsr() {
		return currUsr;
	}

	public void setCurrUsr(CurrentUser currentUser) {
		this.currUsr = currentUser;
	}

	public ToDoBean getNewSubTask() {
		return newSubTask;
	}

	public void setNewSubTask(ToDoBean newSubTask) {
		this.newSubTask = newSubTask;
	}

	public ToDoBean getNewTask() {
		return newTask;
	}

	public void setNewTask(ToDoBean newTask) {
		this.newTask = newTask;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getSubTaskID() {
		return subTaskID;
	}

	public void setSubTaskID(String subTaskID) {
		this.subTaskID = subTaskID;
	}

	public List<ToDoRealizerBean> getRealizers() {
		return realizers;
	}

	public void setRealizers(List<ToDoRealizerBean> realizers) {
		this.realizers = realizers;
	}

	public void setIsAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public boolean getIsAdmin() {
		return isAdmin;
	}

	public void setAllGroups(boolean allGroups) {
		this.allGroups = allGroups;
	}

	public boolean isAllGroups() {
		return allGroups;
	}
	
	
}
