package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.model.extra.ApplicationStatus;
import hr.fer.zemris.jcms.service.ApplicationService;
import hr.fer.zemris.jcms.web.actions.data.ApplicationAdminAproveData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import com.opensymphony.xwork2.Preparable;

@Deprecated
public class ApplicationAdminAprove extends ExtendedActionSupport implements Preparable {

	private static final long serialVersionUID = 2L;

	private ApplicationAdminAproveData data = null;
	
	
	@Override
	public void prepare() throws Exception {
		data = new ApplicationAdminAproveData(MessageLoggerFactory.createMessageLogger(this));
		for(ApplicationStatus as : ApplicationStatus.values()){
			data.getStatuses().put(as.name(), getText(as.name()));
		}
	}

    public String viewStudent() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	ApplicationService.getApplicationAdminAproveData(data, getCurrentUser().getUserID(), "view");
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		return SHOW_FATAL_MESSAGE;
    }

    public String aprove() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	ApplicationService.getApplicationAdminAproveData(data, getCurrentUser().getUserID(), "save");
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			if(data.getFromDefinitionID()!=null) {
				return "success-list";
			} else {
				return SUCCESS;
			}
		}
		return SHOW_FATAL_MESSAGE;
    }

    public String execute() throws Exception {
    	return viewStudent();
    }

	public ApplicationAdminAproveData getData() {
		return data;
	}

	public void setData(ApplicationAdminAproveData data) {
		this.data = data;
	}
}
