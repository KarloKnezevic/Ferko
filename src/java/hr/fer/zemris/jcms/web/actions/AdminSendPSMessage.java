package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.AdminSendPSMessageData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import com.opensymphony.xwork2.Preparable;

public class AdminSendPSMessage extends ExtendedActionSupport implements Preparable {

	private static final long serialVersionUID = 2L;

	private AdminSendPSMessageData data = null;
	
	@Override
	public void prepare() throws Exception {
		data = new AdminSendPSMessageData(MessageLoggerFactory.createMessageLogger(this));
	}

    public String execute() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		BasicBrowsing.getAdminSendPSMessageData(data, getCurrentUser().getUserID());
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
    }

    public AdminSendPSMessageData getData() {
		return data;
	}
    
    public void setData(AdminSendPSMessageData data) {
		this.data = data;
	}
}
