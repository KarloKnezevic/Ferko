package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.StringNameStringValue;
import hr.fer.zemris.jcms.service.SynchronizerService;
import hr.fer.zemris.jcms.web.actions.data.UpdateCourseInstanceRolesData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;
import hr.fer.zemris.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class UpdateCourseInstanceRoles extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private UpdateCourseInstanceRolesData data = null;
	private String text;
	private String semester;
	private List<StringNameStringValue> tasks;
	private String task;
	
	private void prepareTasks() {
		tasks = new ArrayList<StringNameStringValue>();
		tasks.add(new StringNameStringValue("syncCIRoles","Sinkroniziraj uloge po kolegijima"));
	}

	public String getTask() {
		return task;
	}
	
	public void setTask(String task) {
		this.task = task;
	}
	
	public List<StringNameStringValue> getTasks() {
		return tasks;
	}
	
    public String upload() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	prepareTasks();
		data = new UpdateCourseInstanceRolesData(MessageLoggerFactory.createMessageLogger(this));
		if(StringUtil.isStringBlank(getTask())) setTask("syncCIRoles");
		SynchronizerService.getUpdateCourseInstanceRolesData(data, getCurrentUser().getUserID(), getSemester(), text, getTask());
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) {
			return INPUT;
		}
		data.getMessageLogger().registerAsDelayed();
        return SUCCESS;
    }

    public String execute() throws Exception {
    	return input();
    }

    public String input() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	prepareTasks();
		data = new UpdateCourseInstanceRolesData(MessageLoggerFactory.createMessageLogger(this));
		SynchronizerService.getUpdateCourseInstanceRolesData(data, getCurrentUser().getUserID(), getSemester(), null, "input");
        return INPUT;
    }

    public String getSemester() {
		return semester;
	}
    public void setSemester(String semester) {
		this.semester = semester;
	}
    
    public UpdateCourseInstanceRolesData getData() {
		return data;
	}
    public void setData(UpdateCourseInstanceRolesData data) {
		this.data = data;
	}

	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
}
