package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.ext.MarketPlaceBean;
import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.MPGroupsAdminData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

@Deprecated
public class MPGroupsAdmin extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private String courseInstanceID;
	private Long parentID;
	private	MarketPlaceBean bean = new MarketPlaceBean();
	private MPGroupsAdminData data = null;

    public String execute() throws Exception {
    	return input();
    }
    
    public String input() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new MPGroupsAdminData(MessageLoggerFactory.createMessageLogger(this, true));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.getMPGroupsAdminData(data, getCurrentUser().getUserID(), getCourseInstanceID(), getParentID(), getBean(), "input");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
        return INPUT;
    }

    public String update() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new MPGroupsAdminData(MessageLoggerFactory.createMessageLogger(this, true));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.getMPGroupsAdminData(data, getCurrentUser().getUserID(), getCourseInstanceID(), getParentID(), getBean(), "update");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) {
			return INPUT;
		}
        return SUCCESS;
    }

    public String getCourseInstanceID() {
		return courseInstanceID;
	}
    public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
 
    public MPGroupsAdminData getData() {
		return data;
	}
    public void setData(MPGroupsAdminData data) {
		this.data = data;
	}

    public Long getParentID() {
		return parentID;
	}
    public void setParentID(Long parentID) {
		this.parentID = parentID;
	}

    public MarketPlaceBean getBean() {
		return bean;
	}
    public void setBean(MarketPlaceBean bean) {
		this.bean = bean;
	}
}
