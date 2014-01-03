package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.CourseUsersListJSONData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

public class CourseUsersListJSON extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private String courseInstanceID;
	private String relativePath;
	private String user;
	private String userKey;
	
	private CourseUsersListJSONData data = null;
	
    public String execute() throws Exception {
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new CourseUsersListJSONData(MessageLoggerFactory.createMessageLogger(this, true));
    	if(getCourseInstanceID()==null || getCourseInstanceID().equals("") || !hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
    	if(hasCurrentUser()) {
    		BasicBrowsing.getCourseUsersListJSONData(data, getCurrentUser().getUserID(), getCourseInstanceID(), getRelativePath(), getUser());
    	}
        return SUCCESS;
    }

    public String getCourseInstanceID() {
		return courseInstanceID;
	}
    public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
    
    public String getRelativePath() {
		return relativePath;
	}
    public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}
    
    public CourseUsersListJSONData getData() {
		return data;
	}
    public void setData(CourseUsersListJSONData data) {
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
