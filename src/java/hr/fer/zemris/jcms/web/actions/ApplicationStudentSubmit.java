package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.StudentApplicationBean;
import hr.fer.zemris.jcms.service.ApplicationService;
import hr.fer.zemris.jcms.web.actions.data.ApplicationStudentSubmitData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import com.opensymphony.xwork2.Preparable;

@Deprecated
public class ApplicationStudentSubmit extends ExtendedActionSupport implements Preparable {

	private static final long serialVersionUID = 2L;

	private ApplicationStudentSubmitData data = null;
	private StudentApplicationBean bean = null; 
	
	@Override
	public void prepare() throws Exception {
		data = new ApplicationStudentSubmitData(MessageLoggerFactory.createMessageLogger(this));
		bean = new StudentApplicationBean();
	}

    public String newApplication() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	ApplicationService.getApplicationStudentSubmitData(data, bean, getCurrentUser().getUserID(), "new");
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		return SHOW_FATAL_MESSAGE;
    }

    public String saveApplication() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	ApplicationService.getApplicationStudentSubmitData(data, bean, getCurrentUser().getUserID(), "save");
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
    }

    public String execute() throws Exception {
    	return newApplication();
    }

	public ApplicationStudentSubmitData getData() {
		return data;
	}

	public void setData(ApplicationStudentSubmitData data) {
		this.data = data;
	}

	public StudentApplicationBean getBean() {
		return bean;
	}

	public void setBean(StudentApplicationBean bean) {
		this.bean = bean;
	}
	   
}
