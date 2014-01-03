package hr.fer.zemris.jcms.web.actions;

import com.opensymphony.xwork2.Preparable;

import hr.fer.zemris.jcms.beans.ext.AdminListAppealsBean;
import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.AdminListAppealsData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

/**
 * Akcija za ispis liste žalbi na provjeru znanja.
 * 
 * @author Ivan Krišto
 */
public class AdminListAppeals extends ExtendedActionSupport implements Preparable {

	private static final long serialVersionUID = 2L;

	private AdminListAppealsData data = null;
	private AdminListAppealsBean bean = null;
	private String courseInstanceID;
	
	@Override
	public void prepare() throws Exception {
		data = new AdminListAppealsData(MessageLoggerFactory.createMessageLogger(this));
		bean = new AdminListAppealsBean();
	}

    public String execute() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		BasicBrowsing.getAdminListAppealsData(data, bean, getCurrentUser().getUserID(), getCourseInstanceID());
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) return SUCCESS;
		return SHOW_FATAL_MESSAGE;
    }

    public AdminListAppealsData getData() {
		return data;
	}
    public void setData(AdminListAppealsData data) {
		this.data = data;
	}
    
    public AdminListAppealsBean getBean() {
		return bean;
	}
    public void setBean(AdminListAppealsBean bean) {
		this.bean = bean;
	}

	public String getCourseInstanceID() {
		return courseInstanceID;
	}

	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
}
