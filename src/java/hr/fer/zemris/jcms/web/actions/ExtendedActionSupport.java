package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.web.actions.data.support.IMessageContainer;
import hr.fer.zemris.jcms.web.interceptors.data.CurrentUser;
import hr.fer.zemris.jcms.web.interceptors.data.CurrentUserAware;
import hr.fer.zemris.jcms.web.interceptors.data.DelayedMessageProducer;
import hr.fer.zemris.jcms.web.interceptors.data.DelayedMessagesAware;

import com.opensymphony.xwork2.ActionSupport;

public class ExtendedActionSupport extends ActionSupport implements CurrentUserAware, DelayedMessagesAware, DelayedMessageProducer {

	private static final long serialVersionUID = 1L;

	public static final String NO_PERMISSION = "nopermission";
	public static final String NOT_LOGGED_IN = "notLoggedIn";
	public static final String SHOW_FATAL_MESSAGE = "showFatalMessage";
	public static final String WRAPPED_STREAM = "wrapped-stream";
	
	private CurrentUser currentUser;
	private IMessageContainer delayedMessagesContainer;
	private String dmsgid;
	private String title;
	private String description;

	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public void setCurrentUser(CurrentUser currentUser) {
		this.currentUser = currentUser;
	}

	public CurrentUser getCurrentUser() {
		return currentUser;
	}
	
	public boolean hasCurrentUser() {
		return currentUser != null;
	}
	
	public String checkUser(String[] requiredRoles, boolean allRequired) {
    	if(getCurrentUser()==null) {
    		return NOT_LOGGED_IN;
    	}
    	String username = getCurrentUser().getUsername();
    	if(username==null) {
    		return NOT_LOGGED_IN;
    	}
    	if(requiredRoles!=null) {
    		for(int i = 0; i < requiredRoles.length; i++) {
    			if(!getCurrentUser().isUserInRole(requiredRoles[i])) {
    				if(allRequired) return NO_PERMISSION;
    			} else {
    				if(!allRequired) break;
    			}
    		}
    	}
    	return null;
	}
	
	@Override
	public void setDelayedMessagesContainer(
			IMessageContainer delayedMessagesContainer) {
		this.delayedMessagesContainer = delayedMessagesContainer;
	}
	
	@Override
	public IMessageContainer getDelayedMessageContainer() {
		return delayedMessagesContainer;
	}

	public String getDmsgid() {
		return dmsgid;
	}

	public void setDmsgid(String dmsgid) {
		this.dmsgid = dmsgid;
	}
}
