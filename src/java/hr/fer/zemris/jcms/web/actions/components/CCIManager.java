package hr.fer.zemris.jcms.web.actions.components;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import com.opensymphony.xwork2.Preparable;

import hr.fer.zemris.jcms.beans.CourseComponentItemBean;
import hr.fer.zemris.jcms.beans.GroupBean;
import hr.fer.zemris.jcms.beans.ext.BaseUserBean;
import hr.fer.zemris.jcms.beans.ext.ComponentDefBean;
import hr.fer.zemris.jcms.beans.ext.EditItemScoresBean;
import hr.fer.zemris.jcms.beans.ext.FileUploadBean;
import hr.fer.zemris.jcms.beans.ext.GroupOwnerFlat;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.parsers.CourseComponentDefParser;
import hr.fer.zemris.jcms.parsers.json.JSONException;
import hr.fer.zemris.jcms.parsers.json.JSONWriter;
import hr.fer.zemris.jcms.service.CourseComponentService;
import hr.fer.zemris.jcms.web.actions.ExtendedActionSupport;
import hr.fer.zemris.jcms.web.actions.data.CourseComponentData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;
import hr.fer.zemris.util.InputStreamWrapper;
import hr.fer.zemris.util.StringUtil;

public class CCIManager extends ExtendedActionSupport implements Preparable{

	private static final long serialVersionUID = 1L;

	private CourseComponentData data;
	private String id;
	private String groupID;
	private String courseComponentID;
	private String inputData;
	private String jmbag;
	private String groupText;
	
	private CourseComponentItemBean itemBean;
	private EditItemScoresBean editItemScoreBean;
	private FileUploadBean uploadBean;
	private GroupOwnerFlat goBean;
	
	private InputStreamWrapper streamWrapper;
	
	public static final String REDIRECT_SUCCESS = "redirectSuccess";
	public static final String VIEW_SUCCESS = "viewSuccess";
	public static final String REDIRECT_VIEW = "redirectView";
	public static final String VIEW_FILE = "viewFile";
	public static final String REDIRECT_GROUPVIEW = "redirectGroupView";
	public static final String VIEW_EDITDEF = "viewEditDef";
	public static final String VIEW_ITEMSCORES = "viewItemScores";
	public static final String REDIRECT_ITEMSCORES = "redirectItemScores";
	public static final String VIEW_GROUPOWNERS = "viewGroupOwners";
	public static final String REDIRECT_GROUPOWNERS = "redirectGroupOwners";
	public static final String VIEW_USERSCORES = "viewUserScores";
	public static final String REDIRECT_USERVIEW = "redirectUserScores";
	
	@Override
	public void prepare() throws Exception {
		data = new CourseComponentData(MessageLoggerFactory.createMessageLogger(this, true));
		itemBean = new CourseComponentItemBean();
		editItemScoreBean = new EditItemScoresBean();
		uploadBean = new FileUploadBean(); 
		goBean = new GroupOwnerFlat();
	}

	public String getFilterGroupID() {
		return data.getFilterGroupID();
	}
	public void setFilterGroupID(String filterGroupID) {
		data.setFilterGroupID(filterGroupID);
	}
	
	@Override
	public String execute() throws Exception {
		return SHOW_FATAL_MESSAGE;
	}
	
	public String newItem() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.newComponentItem(data, courseComponentID, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		return INPUT;
	}
	
	public String editItem() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.editComponentItem(data, id, itemBean, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		return INPUT;
	}
	
	public String updateItem() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.saveComponentItem(data, courseComponentID, itemBean, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		if (data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		
		data.getMessageLogger().registerAsDelayed();
		return REDIRECT_SUCCESS;
	}
	
	public String viewItem() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.viewComponentItem(data, id, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		return VIEW_SUCCESS;
	}
	
	public String changeDefPosition() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.changeDefPosition(data, id, getCurrentUser().getUserID(),inputData);
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		return REDIRECT_VIEW;
	}
	
	public String viewItemScores() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.viewItemScore(data, id, editItemScoreBean, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		return VIEW_ITEMSCORES;
	}
	
	public String viewGroupScores() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.viewItemScore(data, id, groupID, editItemScoreBean, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		return VIEW_ITEMSCORES;
	}
	
	public String saveItemScores() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.saveItemScore(data, id, editItemScoreBean, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		if (data.getResult().equals(AbstractActionData.RESULT_INPUT)) return VIEW_ITEMSCORES;
		
		data.getMessageLogger().registerAsDelayed();
		return REDIRECT_ITEMSCORES;
	}
	
	public String saveGroupScores() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.saveItemScore(data, id, groupID, editItemScoreBean, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		if (data.getResult().equals(AbstractActionData.RESULT_INPUT)) return VIEW_ITEMSCORES;
		
		data.getMessageLogger().registerAsDelayed();
		return REDIRECT_GROUPVIEW;
	}
	
	public String viewUserScores() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		// Ako nemam postavljen userselection, a nesto imam u history-ju, izvadi to od tamo!
		if(StringUtil.isStringBlank(data.getUserSelection())) {
			String history = data.getMessageLogger().getMessageContainer().getPrivateMessage("userHistory");
			if(!StringUtil.isStringBlank(history)) {
				data.setUserSelection(history);
			}
		}
		CourseComponentService.getUserScore(data, id, groupID, jmbag, editItemScoreBean, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		return VIEW_USERSCORES;
	}
	
	public String saveUserScores() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.saveUserScore(data, id, groupID, editItemScoreBean, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		if (data.getResult().equals(AbstractActionData.RESULT_INPUT)) return VIEW_USERSCORES;
		
		data.getMessageLogger().registerAsDelayed();
		return REDIRECT_USERVIEW;
	}
	
	public String editGroupOwners() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.editGroupOwners(data, id, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		return VIEW_GROUPOWNERS;
	}

	public String addGroupOwnerJSON() throws JSONException, IOException {
		boolean err = false;
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
			err = true;
		} else {
			CourseComponentService.addGroupOwner(data, id, goBean, getCurrentUser().getUserID());
		}
		if (err || data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			StringWriter writer = new StringWriter();
			JSONWriter jw = new JSONWriter(writer).object();
			jw.key("status").value("ERR");
			jw.endObject();
			streamWrapper = InputStreamWrapper.createInputStreamWrapperFromText(
					writer.toString(), 
    				"application/json");
    		return WRAPPED_STREAM;
		} else {
			StringWriter writer = new StringWriter();
			JSONWriter jw = new JSONWriter(writer).object();
			jw.key("status").value("OK");
			jw.key("owner").object();
			// Ovdje stavi korisnika
			jw.key("firstName").value(data.getOwner().getUser().getFirstName());
			jw.key("lastName").value(data.getOwner().getUser().getLastName());
			jw.key("jmbag").value(data.getOwner().getUser().getJmbag());
			jw.key("userID").value(data.getOwner().getId());
			jw.endObject();
			jw.endObject();
			streamWrapper = InputStreamWrapper.createInputStreamWrapperFromText(
					writer.toString(), 
    				"application/json");
    		return WRAPPED_STREAM;
		}
	}
	
	public String removeGroupOwnerJSON() throws JSONException, IOException {
		boolean err = false;
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
			err = true;
		} else {
			CourseComponentService.removeGroupOwner(data, id, groupID, getCurrentUser().getUserID());
		}
		if (err || data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			StringWriter writer = new StringWriter();
			JSONWriter jw = new JSONWriter(writer).object();
			jw.key("status").value("ERR");
			jw.endObject();
			streamWrapper = InputStreamWrapper.createInputStreamWrapperFromText(
					writer.toString(), 
    				"application/json");
    		return WRAPPED_STREAM;
		} else {
			StringWriter writer = new StringWriter();
			JSONWriter jw = new JSONWriter(writer).object();
			jw.key("status").value("OK");
			jw.endObject();
			streamWrapper = InputStreamWrapper.createInputStreamWrapperFromText(
					writer.toString(), 
    				"application/json");
    		return WRAPPED_STREAM;
		}
	}
	

	public String editGroupOwnersJSON() throws JSONException, IOException {
		boolean err = false;
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		err = true;
		} else {
			CourseComponentService.editGroupOwners(data, id, getCurrentUser().getUserID());
		}
		if (err || data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			StringWriter writer = new StringWriter();
			JSONWriter jw = new JSONWriter(writer);
			jw.key("status").value("ERR");
			jw.endObject();
			streamWrapper = InputStreamWrapper.createInputStreamWrapperFromText(
					writer.toString(), 
    				"application/json");
    		return WRAPPED_STREAM;
		}
		StringWriter writer = new StringWriter();
		JSONWriter jw = new JSONWriter(writer).object().key("glist");
		serializeGroupList(jw, data.getGroupList());
		jw.key("status").value("OK");
		jw.key("itemID").value(data.getCourseComponentItem().getId());
		jw.key("users");
		serializeUsersList(jw, data.getUserList());
		jw.endObject();
		streamWrapper = InputStreamWrapper.createInputStreamWrapperFromText(
				writer.toString(), 
				"application/json");
		return WRAPPED_STREAM;
	}
	
	private JSONWriter serializeUsersList(JSONWriter jw, List<User> userList) throws JSONException {
		jw.array();
		for(User u : userList) {
			jw.object();
			jw.key("firstName").value(u.getFirstName());
			jw.key("lastName").value(u.getLastName());
			jw.key("jmbag").value(u.getJmbag());
			jw.key("userID").value(u.getId());
			jw.endObject();
		}
		jw.endArray();
		return jw;
	}

	private JSONWriter serializeGroupList(JSONWriter jw, List<GroupBean> groupList) throws JSONException {
		jw.array();
		for(GroupBean gb : groupList) {
			jw.object();
			jw.key("groupID").value(gb.getId());
			jw.key("groupName").value(gb.getName());
			jw.key("owners");
			serializeOwnersList(jw, gb.getOwnerList());
			jw.endObject();
		}
		jw.endArray();
		return jw;
	}

	private JSONWriter serializeOwnersList(JSONWriter jw, List<BaseUserBean> ownerList) throws JSONException {
		jw.array();
		for(BaseUserBean u : ownerList) {
			jw.object();
			jw.key("firstName").value(u.getFirstName());
			jw.key("lastName").value(u.getLastName());
			jw.key("jmbag").value(u.getJmbag());
			jw.key("userID").value(u.getUserID());
			jw.endObject();
		}
		jw.endArray();
		return jw;
	}

	public String addGroupOwner() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.addGroupOwner(data, id, goBean, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		data.getMessageLogger().registerAsDelayed();
		return REDIRECT_GROUPOWNERS;
	}
	
	public String removeGroupOwner() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.removeGroupOwner(data, id, groupID, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		data.getMessageLogger().registerAsDelayed();
		return REDIRECT_GROUPOWNERS;
	}
	
	public String newItemDef() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.newItemDef(data, id, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		return VIEW_EDITDEF;
	}
	
	public String saveItemDef() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		List<ComponentDefBean> beanList = null;
		try {
			beanList = CourseComponentDefParser.parse(new StringReader(inputData));
		} catch (Exception ex) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.importParseError")+" "+ex.getMessage());
			CourseComponentService.newItemDef(data, id, getCurrentUser().getUserID());
			if (data.getResult().equals(AbstractActionData.RESULT_FATAL))
				return SHOW_FATAL_MESSAGE;
			
			return VIEW_EDITDEF;
		}
		CourseComponentService.saveItemDef(data, id, beanList, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		if (data.getResult().equals(AbstractActionData.RESULT_INPUT)) return VIEW_EDITDEF;
		
		return REDIRECT_VIEW;
	}
	
	public String removeItemDef() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.removeItemDef(data, id, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		data.getMessageLogger().registerAsDelayed();
		return REDIRECT_VIEW;
	}
	
	public String createComponentItemGroups() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.createComponentItemGroups(data, id, getCurrentUser().getUserID(), getGroupText());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		data.getMessageLogger().registerAsDelayed();
		return REDIRECT_VIEW;
	}
	
	public String uploadFile() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.uploadItemDescriptionFile(data, id, uploadBean, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		if (data.getResult().equals(AbstractActionData.RESULT_INPUT)) return VIEW_SUCCESS;
		
		data.getMessageLogger().registerAsDelayed();
		return REDIRECT_VIEW;
	}
	
	public String removeFile() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.removeItemDescriptionFile(data, id, getCurrentUser().getUserID());
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) return SHOW_FATAL_MESSAGE;
		
		data.getMessageLogger().registerAsDelayed();
		return REDIRECT_VIEW;
	}
	
	public String viewFile() {
		if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
		}
		CourseComponentService.viewItemDescriptionFile(data, id, getCurrentUser().getUserID());
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

	public CourseComponentItemBean getItemBean() {
		return itemBean;
	}

	public void setItemBean(CourseComponentItemBean itemBean) {
		this.itemBean = itemBean;
	}

	public String getCourseComponentID() {
		return courseComponentID;
	}

	public void setCourseComponentID(String courseComponentID) {
		this.courseComponentID = courseComponentID;
	}

	public FileUploadBean getUploadBean() {
		return uploadBean;
	}

	public void setUploadBean(FileUploadBean uploadBean) {
		this.uploadBean = uploadBean;
	}

	public String getInputData() {
		return inputData;
	}

	public void setInputData(String inputData) {
		this.inputData = inputData;
	}

	public EditItemScoresBean getEditItemScoreBean() {
		return editItemScoreBean;
	}

	public void setEditItemScoreBean(EditItemScoresBean editItemScoreBean) {
		this.editItemScoreBean = editItemScoreBean;
	}

	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	public GroupOwnerFlat getGoBean() {
		return goBean;
	}

	public void setGoBean(GroupOwnerFlat goBean) {
		this.goBean = goBean;
	}

	public String getJmbag() {
		return jmbag;
	}

	public void setJmbag(String jmbag) {
		this.jmbag = jmbag;
	}
	
	public String getGroupText() {
		return groupText;
	}

	public void setGroupText(String groupText) {
		this.groupText = groupText;
	}
	
	public InputStreamWrapper getStreamWrapper() {
		return streamWrapper;
	}
}
