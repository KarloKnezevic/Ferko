package hr.fer.zemris.jcms.web.actions.components;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;


import hr.fer.zemris.jcms.beans.CourseComponentTaskBean;
import hr.fer.zemris.jcms.beans.ext.AssessmentAssistantBean;
import hr.fer.zemris.jcms.beans.ext.FileDownloadBean;
import hr.fer.zemris.jcms.beans.ext.ReviewersUserTaskBean;
import hr.fer.zemris.jcms.beans.ext.TaskFileUploadBean;
import hr.fer.zemris.jcms.beans.ext.TaskReviewBean;
import hr.fer.zemris.jcms.parsers.UserImportParser;
import hr.fer.zemris.jcms.service.CourseComponentService;
import hr.fer.zemris.jcms.web.actions.ExtendedActionSupport;
import hr.fer.zemris.jcms.web.actions.data.CourseComponentData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;
import hr.fer.zemris.util.InputStreamWrapper;

import com.opensymphony.xwork2.Preparable;

public class CCTManager extends ExtendedActionSupport implements Preparable {

	private static final long serialVersionUID = 1L;

	private CourseComponentData data;
	private String id;
	private String courseComponentItemID;
	private String importData;
	private String userID;
	private String sureToRemove;
	private InputStreamWrapper streamWrapper;
	
	private CourseComponentTaskBean taskBean;
	private TaskFileUploadBean taskFileBean;
	private FileDownloadBean fileBean;
	private TaskReviewBean reviewBean;
	private List<AssessmentAssistantBean> reviewersList;
	private List<ReviewersUserTaskBean> taskUsersList;
	
	public static final String REDIRECT_SUCCESS = "redirectSuccess";
	public static final String REDIRECT_ITEM = "redirectItem";
	public static final String TASK_ASSIGN_IMPORT = "taskAssignImport";
	public static final String VIEW_REVIEWERS = "viewReviewers";
	public static final String REDIRECT_REVIEWERS = "redirectReviewers";
	public static final String VIEW_TASK_USERS = "viewTaskUsers";
	public static final String VIEW_FILE = "viewFile";
	public static final String VIEW_ASSIGNMENT = "viewAssignment";
	public static final String REDIRECT_ASSIGNMENT = "redirectAssignment";
	public static final String VIEW_TASKINFO = "viewTaskInfo";
	public static final String REDIRECT_TASKINFO = "redirectTaskInfo";
	public static final String SHOW_MATRIX = "showMatrix";
	
	@Override
	public void prepare() throws Exception {
		data = new CourseComponentData(MessageLoggerFactory.createMessageLogger(this, true));
		taskBean = new CourseComponentTaskBean();
		taskFileBean = new TaskFileUploadBean();
		fileBean = new FileDownloadBean();
		reviewBean = new TaskReviewBean();
		reviewersList = new ArrayList<AssessmentAssistantBean>();
		taskUsersList = new ArrayList<ReviewersUserTaskBean>();
	}
	
	public String getFilterGroupID() {
		return data.getFilterGroupID();
	}
	public void setFilterGroupID(String filterGroupID) {
		data.setFilterGroupID(filterGroupID);
	}

	public String newTask() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.newTask(data, courseComponentItemID, taskBean, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		return INPUT;
	}
	
	public String editTask() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.editTask(data, id, taskBean, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		return INPUT;
	}
	
	public String updateTask() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.saveTask(data, courseComponentItemID, taskBean, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		if (data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		
		data.getMessageLogger().registerAsDelayed();
		return REDIRECT_TASKINFO;
	}
	
	public String removeTask() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.removeTask(data, id, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		return REDIRECT_ITEM;
	}
	
	public String autoAssignTask() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.autoAssignTask(data, id, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		data.getMessageLogger().registerAsDelayed();
		return REDIRECT_TASKINFO;
	}
	
	public String showMatrix() throws Exception {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.showTaskMatrix(data, id, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		return SHOW_MATRIX;
	}

	public String matrixAddItem() throws Exception {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		InputStreamWrapper[] wrapper = new InputStreamWrapper[1];
		CourseComponentService.cctMatrixManipulateItem(data, id, userID, getCurrentUser().getUserID(), "add", null, wrapper);
		streamWrapper = wrapper[0];
		return "wrapped-stream";
	}
	
	public String matrixRemoveItem() throws Exception {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		InputStreamWrapper[] wrapper = new InputStreamWrapper[1];
		CourseComponentService.cctMatrixManipulateItem(data, id, userID, getCurrentUser().getUserID(), "remove", getSureToRemove(), wrapper);
		streamWrapper = wrapper[0];
		return "wrapped-stream";
	}

	public String newAssignTask() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.assignTaskUsers(data, id, null, getCurrentUser().getUserID(), "new");
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		return TASK_ASSIGN_IMPORT;
	}
	
	public String assignTask() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		
		List<String> beanList = null;
		if (importData != null) {
			try { 
				beanList = UserImportParser.parseJmbags(new StringReader(importData));
			} catch(IOException ex) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.internalError"));
				return SHOW_FATAL_MESSAGE;
			}
			CourseComponentService.assignTaskUsers(data, id, beanList, getCurrentUser().getUserID(), "edit");
			if (data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
				data.getMessageLogger().registerAsDelayed();
				return REDIRECT_TASKINFO;
			}
			if (data.getResult().equals(AbstractActionData.RESULT_FATAL))
				return SHOW_FATAL_MESSAGE;
			if (data.getResult().equals(AbstractActionData.RESULT_INPUT))
				return TASK_ASSIGN_IMPORT;
		}
		CourseComponentService.assignTaskUsers(data, id, null, getCurrentUser().getUserID(), "new");
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		return TASK_ASSIGN_IMPORT;
	}
	
	public String viewAssignmentStatus() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.viewAssignmentStatus(data, id, reviewBean, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		return VIEW_ASSIGNMENT;
	}
	
	public String reviewAssignment() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.reviewAssignment(data, id, reviewBean, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		if (data.getResult().equals(AbstractActionData.RESULT_INPUT)) return VIEW_ASSIGNMENT;
		
		data.getMessageLogger().registerAsDelayed();
		return REDIRECT_ASSIGNMENT;
	}
	
	public String viewReviewers() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.editReviewers(data, id, reviewersList, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
	
		return VIEW_REVIEWERS;
	}
	
	public String updateReviewers() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.saveReviewers(data, id, reviewersList, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		data.getMessageLogger().registerAsDelayed();
		return REDIRECT_REVIEWERS;
	}
	
	public String viewTaskUsers() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.viewTaskUsers(data, id, taskUsersList, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
	
		return VIEW_TASK_USERS;
	}
	
	public String getZipFile() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.getAllTaskFiles(data, id, fileBean, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
	
		return VIEW_FILE;
	}
	
	public String viewTaskInfo() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.viewTaskInfo(data, id, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
	
		return VIEW_TASKINFO;
	}
	
	public String viewUserTask() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.viewTask(data, id, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
	
		return SUCCESS;
	}
	
	public String lockAssignment() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.lockAssignment(data, id, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		if (data.getResult().equals(AbstractActionData.RESULT_INPUT)) return SUCCESS;
		
		data.getMessageLogger().registerAsDelayed();
		return REDIRECT_SUCCESS;
	}
	
	public String unlockAssignment() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.unlockAssignment(data, id, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		data.getMessageLogger().registerAsDelayed();
		return REDIRECT_ASSIGNMENT;
	}

	public String uploadFile() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.uploadTaskFile(data, id, taskFileBean, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		if (data.getResult().equals(AbstractActionData.RESULT_INPUT)) return SUCCESS;
	
		data.getMessageLogger().registerAsDelayed();
		return REDIRECT_SUCCESS;
	}
	
	public String removeFile() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.removeTaskFile(data, id, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
	
		data.getMessageLogger().registerAsDelayed();
		return REDIRECT_SUCCESS;
	}
	public String viewFile() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.viewTaskFile(data, id, fileBean, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
	
		return VIEW_FILE;
	}

	public CourseComponentData getData() {
		return data;
	}

	public void setData(CourseComponentData data) {
		this.data = data;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CourseComponentTaskBean getTaskBean() {
		return taskBean;
	}

	public void setTaskBean(CourseComponentTaskBean taskBean) {
		this.taskBean = taskBean;
	}

	public TaskFileUploadBean getTaskFileBean() {
		return taskFileBean;
	}

	public void setTaskFileBean(TaskFileUploadBean taskFileBean) {
		this.taskFileBean = taskFileBean;
	}

	public String getCourseComponentItemID() {
		return courseComponentItemID;
	}

	public void setCourseComponentItemID(String courseComponentItemID) {
		this.courseComponentItemID = courseComponentItemID;
	}

	public String getImportData() {
		return importData;
	}

	public void setImportData(String importData) {
		this.importData = importData;
	}

	public List<AssessmentAssistantBean> getReviewersList() {
		return reviewersList;
	}

	public void setReviewersList(List<AssessmentAssistantBean> reviewersList) {
		this.reviewersList = reviewersList;
	}

	public List<ReviewersUserTaskBean> getTaskUsersList() {
		return taskUsersList;
	}

	public void setTaskUsersList(List<ReviewersUserTaskBean> taskUsersList) {
		this.taskUsersList = taskUsersList;
	}

	public FileDownloadBean getFileBean() {
		return fileBean;
	}

	public void setFileBean(FileDownloadBean fileBean) {
		this.fileBean = fileBean;
	}

	public TaskReviewBean getReviewBean() {
		return reviewBean;
	}

	public void setReviewBean(TaskReviewBean reviewBean) {
		this.reviewBean = reviewBean;
	}
	
	public void setUserID(String userID) {
		this.userID = userID;
	}
	
	public InputStreamWrapper getStreamWrapper() {
		return streamWrapper;
	}
	
	public String getSureToRemove() {
		return sureToRemove;
	}
	
	public void setSureToRemove(String sureToRemove) {
		this.sureToRemove = sureToRemove;
	}
}
