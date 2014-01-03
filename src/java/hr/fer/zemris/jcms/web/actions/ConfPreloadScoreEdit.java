package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.ext.ConfPreloadScoreEditBean;
import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.ConfPreloadScoreEditData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import com.opensymphony.xwork2.Preparable;

public class ConfPreloadScoreEdit extends ExtendedActionSupport implements Preparable {

	private static final long serialVersionUID = 2L;

	private ConfPreloadScoreEditData data = null;
	private ConfPreloadScoreEditBean bean = null;
	
	@Override
	public void prepare() throws Exception {
		data = new ConfPreloadScoreEditData(MessageLoggerFactory.createMessageLogger(this, true));
		bean = new ConfPreloadScoreEditBean();
	}

    public String edit() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		BasicBrowsing.getConfPreloadScoreEditData(data, bean, getCurrentUser().getUserID(), "edit");
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		return SHOW_FATAL_MESSAGE;
    }

    public String save() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		BasicBrowsing.getConfPreloadScoreEditData(data, bean, getCurrentUser().getUserID(), "save");
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			data.getMessageLogger().registerAsDelayed();
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
    }

    public String pickLetter() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		BasicBrowsing.getConfPreloadScoreEditData(data, bean, getCurrentUser().getUserID(), "pickLetter");
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		return SHOW_FATAL_MESSAGE;
    }

    public String execute() throws Exception {
    	return edit();
    }

    public ConfPreloadScoreEditData getData() {
		return data;
	}
    public void setData(ConfPreloadScoreEditData data) {
		this.data = data;
	}
    
    public ConfPreloadScoreEditBean getBean() {
		return bean;
	}
    public void setBean(ConfPreloadScoreEditBean bean) {
		this.bean = bean;
	}
}
