package hr.fer.zemris.jcms.web.actions;

import java.util.Map;

import org.apache.struts2.interceptor.SessionAware;

public class Logout extends ExtendedActionSupport implements SessionAware {

	private static final long serialVersionUID = 1L;

	private Map<String,Object> sessionMap;
	
	@SuppressWarnings("unchecked")
	@Override
	public void setSession(Map map) {
		this.sessionMap = (Map<String,Object>)map;
	}

	public String execute() throws Exception {
        sessionMap.remove("jcms_currentUserID");
        sessionMap.remove("jcms_currentUserUsername");
        sessionMap.remove("jcms_currentUserRoles");
        sessionMap.remove("jcms_currentUserLastName");
        sessionMap.remove("jcms_currentUserFirstName");
		return SUCCESS;
    }
}
