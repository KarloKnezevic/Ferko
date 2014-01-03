package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.ext.MPViewBean;
import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.MPViewData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

@Deprecated
public class MPView extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private String courseInstanceID;
	private Long parentID;
	private	MPViewBean bean = new MPViewBean();
	private MPViewData data = null;

    public String execute() throws Exception {
    	return view();
    }
    
    public String view() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new MPViewData(MessageLoggerFactory.createMessageLogger(this, true));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.getMPViewData(data, getCurrentUser().getUserID(), getCourseInstanceID(), getParentID(), getBean(), "view");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
        return SUCCESS;
    }

    public String getCourseInstanceID() {
		return courseInstanceID;
	}
    public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
 
    public MPViewData getData() {
		return data;
	}
    public void setData(MPViewData data) {
		this.data = data;
	}

    public Long getParentID() {
		return parentID;
	}
    public void setParentID(Long parentID) {
		this.parentID = parentID;
	}

    public MPViewBean getBean() {
		return bean;
	}
    public void setBean(MPViewBean bean) {
		this.bean = bean;
	}
}
