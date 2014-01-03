package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.ext.ConfProblemsScoreEditBean;
import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.ConfProblemsScoreEditData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import com.opensymphony.xwork2.Preparable;

public class ConfProblemsScoreEdit extends ExtendedActionSupport implements Preparable {

	private static final long serialVersionUID = 2L;

	private ConfProblemsScoreEditData data = null;
	private ConfProblemsScoreEditBean bean = null;
	
	@Override
	public void prepare() throws Exception {
		data = new ConfProblemsScoreEditData(MessageLoggerFactory.createMessageLogger(this, true));
		bean = new ConfProblemsScoreEditBean();
	}

    public String edit() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		BasicBrowsing.getConfProblemsScoreEditData(data, bean, getCurrentUser().getUserID(), "edit");
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		return SHOW_FATAL_MESSAGE;
    }

    public String save() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		BasicBrowsing.getConfProblemsScoreEditData(data, bean, getCurrentUser().getUserID(), "save");
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
		BasicBrowsing.getConfProblemsScoreEditData(data, bean, getCurrentUser().getUserID(), "pickLetter");
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		return SHOW_FATAL_MESSAGE;
    }

    public String execute() throws Exception {
    	return edit();
    }

    public ConfProblemsScoreEditData getData() {
		return data;
	}
    public void setData(ConfProblemsScoreEditData data) {
		this.data = data;
	}
    
    public ConfProblemsScoreEditBean getBean() {
		return bean;
	}
    public void setBean(ConfProblemsScoreEditBean bean) {
		this.bean = bean;
	}
}
