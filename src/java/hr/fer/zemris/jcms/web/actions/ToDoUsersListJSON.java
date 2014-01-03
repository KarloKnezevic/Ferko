package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.ToDoService;
import hr.fer.zemris.jcms.web.actions.data.ToDoUsersListJSONData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

public class ToDoUsersListJSON extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;
	
	private ToDoUsersListJSONData data = null;
	private String user;
	private String userKey;
	
    public String execute() throws Exception {
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new ToDoUsersListJSONData(MessageLoggerFactory.createMessageLogger(this, true));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
    	if(hasCurrentUser()) { 
    		ToDoService.getUsersListJSONData(data, getCurrentUser().getUserID(), getUser());
    	}
        return SUCCESS;
    }

    public ToDoUsersListJSONData getData() {
		return data;
	}
    public void setData(ToDoUsersListJSONData data) {
		this.data = data;
	}
    
    public String getUser() {
		return user;
	}
    public void setUser(String user) {
		this.user = user;
	}
    
    public String getUserKey() {
		return userKey;
	}
    public void setUserKey(String userKey) {
		this.userKey = userKey;
	}
}
