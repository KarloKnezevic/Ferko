package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.SSOService;
import hr.fer.zemris.jcms.web.actions.data.SSOData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;
import hr.fer.zemris.util.StringUtil;

import java.util.Collections;
import java.util.Map;

import org.apache.struts2.interceptor.SessionAware;

public class SSO extends ExtendedActionSupport implements SessionAware {

	private static final long serialVersionUID = 2L;

	private SSOData data;
	private String code;
	private String course;
	private String time;
	private String auth;
	private String system;
	private Map<String,Object> sessionMap;
	
	@SuppressWarnings("unchecked")
	@Override
	public void setSession(Map map) {
		this.sessionMap = (Map<String,Object>)map;
	}
	
    public String execute() throws Exception {
    	if(hasCurrentUser()) {
    		return "alreadyLoggedIn";
    	}
    	if(StringUtil.isStringBlank(code)||StringUtil.isStringBlank(time)||StringUtil.isStringBlank(auth)) {
    		return NOT_LOGGED_IN;
    	}
    	data = new SSOData(MessageLoggerFactory.createMessageLogger(this,true));
    	if(course==null) course="";
		SSOService.getSSOData(data, getCode(), getCourse(), getTime(), getAuth(), getSystem());
		if(!data.isValid()) {
    		return NOT_LOGGED_IN;
		}

		sessionMap.put("jcms_currentUserID", data.getUserID());
        sessionMap.put("jcms_currentUserUsername", data.getUsername());
        sessionMap.put("jcms_currentUserRoles", Collections.unmodifiableSet(data.getRoles()));
        sessionMap.put("jcms_currentUserLastName", data.getLastName());
        sessionMap.put("jcms_currentUserFirstName", data.getFirstName());

        if(data.getCourseInstance()!=null) {
        	return "gotoCourse";
        }
        
        return "gotoMain";
    }

	public SSOData getData() {
		return data;
	}

	public void setData(SSOData data) {
		this.data = data;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCourse() {
		return course;
	}

	public void setCourse(String course) {
		this.course = course;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getAuth() {
		return auth;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}
    
	public String getSystem() {
		return system;
	}
	
	public void setSystem(String system) {
		this.system = system;
	}
    
}
