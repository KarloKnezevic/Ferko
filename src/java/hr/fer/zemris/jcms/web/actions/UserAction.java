package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.UserBean;
import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.UserActionData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

public class UserAction extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private UserBean bean = new UserBean(true);
	private UserActionData data;

    public String execute() throws Exception {
    	return fillSearch();
    }
    
    public String fillSearch() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	data = new UserActionData(MessageLoggerFactory.createMessageLogger(this,true));
		BasicBrowsing.getUserActionData(data, getCurrentUser().getUserID(), bean,"fillSearch");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
        return "searchForm";
    }

    public String find() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	data = new UserActionData(MessageLoggerFactory.createMessageLogger(this,true));
		BasicBrowsing.getUserActionData(data, getCurrentUser().getUserID(), bean,"find");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) {
			return "searchForm";
		}
        return INPUT;
    }

    public String fillNew() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	data = new UserActionData(MessageLoggerFactory.createMessageLogger(this,true));
		BasicBrowsing.getUserActionData(data, getCurrentUser().getUserID(), bean,"fillNew");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
        return INPUT;
    }

    public String update() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	data = new UserActionData(MessageLoggerFactory.createMessageLogger(this,true));
		BasicBrowsing.getUserActionData(data, getCurrentUser().getUserID(), bean,"update");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) {
			return INPUT;
		}
		data.getMessageLogger().registerAsDelayed();
        return SUCCESS;
    }

    public String resetExternalID() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	data = new UserActionData(MessageLoggerFactory.createMessageLogger(this,true));
		BasicBrowsing.getUserActionData(data, getCurrentUser().getUserID(), bean,"resetExternalID");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) {
			return "searchForm";
		}
        return INPUT;
    }

    public UserActionData getData() {
		return data;
	}
    public void setData(UserActionData data) {
		this.data = data;
	}
    
    public UserBean getBean() {
		return bean;
	}
    public void setBean(UserBean bean) {
		this.bean = bean;
	}
}
