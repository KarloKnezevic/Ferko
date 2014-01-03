package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.SeminarRootEditData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import com.opensymphony.xwork2.Preparable;

public class SeminarRootEdit extends ExtendedActionSupport implements Preparable {

	private static final long serialVersionUID = 2L;

	private SeminarRootEditData data = null;
	
	@Override
	public void prepare() throws Exception {
		data = new SeminarRootEditData(MessageLoggerFactory.createMessageLogger(this));
	}

    public String newSeminarRoot() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		BasicBrowsing.getSeminarRootEditData(data, getCurrentUser().getUserID(), "newSeminarRoot");
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		return SHOW_FATAL_MESSAGE;
    }

    public String listSeminarRoots() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		BasicBrowsing.getSeminarRootEditData(data, getCurrentUser().getUserID(), "listSeminarRoots");
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return "list";
		return SHOW_FATAL_MESSAGE;
    }

    public String editSeminarRoot() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		BasicBrowsing.getSeminarRootEditData(data, getCurrentUser().getUserID(), "editSeminarRoot");
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		return SHOW_FATAL_MESSAGE;
    }

    public String saveSeminarRoot() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		BasicBrowsing.getSeminarRootEditData(data, getCurrentUser().getUserID(), "saveSeminarRoot");
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
    }

    public String execute() throws Exception {
    	return newSeminarRoot();
    }

    public SeminarRootEditData getData() {
		return data;
	}
    
    public void setData(SeminarRootEditData data) {
		this.data = data;
	}
}
