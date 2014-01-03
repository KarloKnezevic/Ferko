package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.ExternalGoToLabosiSSOData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

public class ExternalGoToLabosiSSO extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private String courseInstanceID;
	private ExternalGoToLabosiSSOData data = null;
	
    public String execute() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new ExternalGoToLabosiSSOData(MessageLoggerFactory.createMessageLogger(this));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.getExternalGoToLabosiSSOData(data, getCurrentUser().getUserID(), getCourseInstanceID());
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
    
    public ExternalGoToLabosiSSOData getData() {
		return data;
	}
    
    public void setData(ExternalGoToLabosiSSOData data) {
		this.data = data;
	}
}
