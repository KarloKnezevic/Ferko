package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.UserLogin;
import hr.fer.zemris.jcms.service.UserLogin.UserLoginStatus;

import java.util.Collections;
import java.util.Map;

import org.apache.struts2.interceptor.SessionAware;

import com.opensymphony.xwork2.ActionSupport;

public class Login extends ActionSupport implements SessionAware {

	private Map<String,Object> sessionMap;
	
	@SuppressWarnings("unchecked")
	@Override
	public void setSession(Map map) {
		this.sessionMap = (Map<String,Object>)map;
	}
	
	private static final long serialVersionUID = 1L;

	public String execute() throws Exception {
		clearErrorsAndMessages();

		if(!"yes".equals(getAttempt())) {
			return INPUT;
		}

		boolean hasErr = false;
		
        if (isInvalid(getUsername())) {
        	hasErr = true;
        	addFieldError("username", "Username is required field!");
        }

        if (isInvalid(getPassword())) {
        	hasErr = true;
        	addFieldError("password", "Password is required field!");
        }

        if(hasErr) {
        	return INPUT;
        }
        
        UserLogin.UserData udata = checkCurrentUser();
        if(udata == null) {
        	addActionError("Invalid username or password!");
        	return INPUT;
        }
        if(udata.getStatus()==UserLoginStatus.INVALID) {
        	addActionError("Invalid username or password!");
        	return INPUT;
        }
        if(udata.getStatus()==UserLoginStatus.LOCKED) {
        	addActionError("Account is locked.");
        	return INPUT;
        }
        if(udata.getStatus()==UserLoginStatus.INCOMPLETE) {
        	addActionError("Account data is incomplete.");
        	return INPUT;
        }
        if(udata.getStatus()!=UserLoginStatus.SUCCESS) {
        	addActionError("Unexpected response. Developers fault!");
        	return INPUT;
        }

        sessionMap.put("jcms_currentUserID", udata.getUserID());
        sessionMap.put("jcms_currentUserUsername", udata.getUsername());
        sessionMap.put("jcms_currentUserRoles", Collections.unmodifiableSet(udata.getRoles()));
        sessionMap.put("jcms_currentUserLastName", udata.getLastName());
        sessionMap.put("jcms_currentUserFirstName", udata.getFirstName());
        sessionMap.put("jcms_currentUserJmbag", udata.getJmbag());

        return SUCCESS;
    }

	private UserLogin.UserData checkCurrentUser() {
    	return UserLogin.checkUser(getUsername(), getPassword());
	}

	private boolean isInvalid(String value) {
        return (value == null || value.length() == 0);
    }

    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    private String attempt;

    public String getAttempt() {
		return attempt;
	}
    
    public void setAttempt(String attempt) {
		this.attempt = attempt;
	}

}
