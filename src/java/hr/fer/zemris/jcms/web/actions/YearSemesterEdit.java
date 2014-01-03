package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.YearSemesterBean;
import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.YearSemesterEditData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

public class YearSemesterEdit extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private YearSemesterEditData data = null;
	private YearSemesterBean bean = new YearSemesterBean(); 
	private boolean create = false;
	
    public String newYS() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	create = true;
		data = new YearSemesterEditData(MessageLoggerFactory.createMessageLogger(this));
    	BasicBrowsing.getYearSemesterEditData(data, bean, getCurrentUser().getUserID(), "new", isCreate());
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		return SHOW_FATAL_MESSAGE;
    }

    public String listYS() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	create = false;
		data = new YearSemesterEditData(MessageLoggerFactory.createMessageLogger(this));
    	BasicBrowsing.getYearSemesterEditData(data, bean, getCurrentUser().getUserID(), "list", isCreate());
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return "list";
		return SHOW_FATAL_MESSAGE;
    }

    public String editYS() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	create = false;
		data = new YearSemesterEditData(MessageLoggerFactory.createMessageLogger(this));
		BasicBrowsing.getYearSemesterEditData(data, bean, getCurrentUser().getUserID(), "edit", isCreate());
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		return SHOW_FATAL_MESSAGE;
    }

    public String saveYS() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		data = new YearSemesterEditData(MessageLoggerFactory.createMessageLogger(this));
		BasicBrowsing.getYearSemesterEditData(data, bean, getCurrentUser().getUserID(), "save", isCreate());
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
    }

    public String execute() throws Exception {
    	return newYS();
    }

    public YearSemesterEditData getData() {
		return data;
	}
    public void setData(YearSemesterEditData data) {
		this.data = data;
	}
    
    public YearSemesterBean getBean() {
		return bean;
	}
    public void setBean(YearSemesterBean bean) {
		this.bean = bean;
	}
    
    public boolean isCreate() {
		return create;
	}
    public void setCreate(boolean create) {
		this.create = create;
	}
}
