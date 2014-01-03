package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.ext.CourseUserPermissionsBean;
import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.EditCoursePermissionsData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;
import hr.fer.zemris.util.StringUtil;

public class EditCoursePermissions extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private String courseInstanceID;
	
	private EditCoursePermissionsData data = null;
	private CourseUserPermissionsBean bean = new CourseUserPermissionsBean();
	// Popunit ce se kod dodavanja novog korisnika
	private String user;
	
	// Vraca glavnu stranicu koja sadrzi s:div za ispis trenutnih dozvola i formular za pretrazivanje korisnika
    public String execute() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new EditCoursePermissionsData(MessageLoggerFactory.createMessageLogger(this, true));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.getEditCoursePermissionsData(data, bean, getCurrentUser().getUserID(), getCourseInstanceID(), "list");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
        return SUCCESS;
    }

    // dodaje novog korisnika i njegove dozvole; nanovo renderira citavu stranicu
    public String add() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new EditCoursePermissionsData(MessageLoggerFactory.createMessageLogger(this, true));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
    	bean.getNewUser().setId(StringUtil.isStringBlank(user) ? null : Long.valueOf(user));
		BasicBrowsing.getEditCoursePermissionsData(data, bean, getCurrentUser().getUserID(), getCourseInstanceID(), "add");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
		data.getMessageLogger().registerAsDelayed();
        return "redirect";
    }
    
    // azurira sve korisnike i njihove dozvole; nanovo renderira citavu stranicu
    public String update() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new EditCoursePermissionsData(MessageLoggerFactory.createMessageLogger(this, true));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.getEditCoursePermissionsData(data, bean, getCurrentUser().getUserID(), getCourseInstanceID(), "update");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
		data.getMessageLogger().registerAsDelayed();
        return "redirect";
    }
    
    public String getCourseInstanceID() {
		return courseInstanceID;
	}
    public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
    
    public EditCoursePermissionsData getData() {
		return data;
	}
    public void setData(EditCoursePermissionsData data) {
		this.data = data;
	}

	public CourseUserPermissionsBean getBean() {
	   return bean;
	}
	public void setBean(CourseUserPermissionsBean bean) {
	   this.bean = bean;
	}
	
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
}
