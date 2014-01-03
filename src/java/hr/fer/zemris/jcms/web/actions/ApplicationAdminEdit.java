package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.ApplicationDefinitionBean;
import hr.fer.zemris.jcms.service.ApplicationService;
import hr.fer.zemris.jcms.web.actions.data.ApplicationAdminEditData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import com.opensymphony.xwork2.Preparable;

@Deprecated
public class ApplicationAdminEdit extends ExtendedActionSupport implements Preparable {

	private static final long serialVersionUID = 2L;

	private ApplicationAdminEditData data = null;
	private ApplicationDefinitionBean bean = null; 
	
	@Override
	public void prepare() throws Exception {
		data = new ApplicationAdminEditData(MessageLoggerFactory.createMessageLogger(this));
		bean = new ApplicationDefinitionBean();
	}

    public String newDefinition() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	ApplicationService.getApplicationAdminEditData(data, bean, getCurrentUser().getUserID(), "new");
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		return SHOW_FATAL_MESSAGE;
    }

    public String editDefinition() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	ApplicationService.getApplicationAdminEditData(data, bean, getCurrentUser().getUserID(), "edit");
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		return SHOW_FATAL_MESSAGE;
    }

    public String saveDefinition() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	ApplicationService.getApplicationAdminEditData(data, bean, getCurrentUser().getUserID(), "save");
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
    }

    public String execute() throws Exception {
    	return newDefinition();
    }

    public ApplicationAdminEditData getData() {
		return data;
	}
    public void setData(ApplicationAdminEditData data) {
		this.data = data;
	}
    
    public ApplicationDefinitionBean getBean() {
		return bean;
	}
    public void setBean(ApplicationDefinitionBean bean) {
		this.bean = bean;
	}
}
