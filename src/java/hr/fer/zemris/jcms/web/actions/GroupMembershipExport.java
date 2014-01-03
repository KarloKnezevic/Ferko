package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.ext.GroupMembershipExportBean;
import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.GroupMembershipExportData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

public class GroupMembershipExport extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private GroupMembershipExportData data = null;
	private GroupMembershipExportBean bean = new GroupMembershipExportBean();

    public String execute() throws Exception {
    	return input();
    }
    
    public String view() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new GroupMembershipExportData(MessageLoggerFactory.createMessageLogger(this, true));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.getGroupMembershipExportData(data, getCurrentUser().getUserID(), getBean(), "view");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
		if(data.getResult().equals(AbstractActionData.RESULT_NONFATAL_ERROR)) {
			return INPUT;
		}
        return SUCCESS;
    }

    public String input() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new GroupMembershipExportData(MessageLoggerFactory.createMessageLogger(this, true));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.getGroupMembershipExportData(data, getCurrentUser().getUserID(), getBean(), "input");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
        return INPUT;
    }

    public GroupMembershipExportData getData() {
		return data;
	}
    public void setData(GroupMembershipExportData data) {
		this.data = data;
	}

    public GroupMembershipExportBean getBean() {
		return bean;
	}
    public void setBean(GroupMembershipExportBean bean) {
		this.bean = bean;
	}
}
