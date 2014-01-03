package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.ext.GroupOwnershipBean;
import hr.fer.zemris.jcms.security.JCMSSecurityConstants;
import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.GroupOwnershipData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class GroupOwnership extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private GroupOwnershipData data = null;
	private GroupOwnershipBean bean = null;
	private String relativePath;
	
    public String execute() throws Exception {
    	return input();
    }
    
    public String input() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new GroupOwnershipData(MessageLoggerFactory.createMessageLogger(this, true));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		Set<String> usersFromGroups = new HashSet<String>();
		usersFromGroups.add(JCMSSecurityConstants.NASTAVNIK);
		usersFromGroups.add(JCMSSecurityConstants.NOSITELJ);
		if(relativePath==null || relativePath.equals("")) relativePath="0";
		BasicBrowsing.getGroupOwnershipData(data, getCurrentUser().getUserID(), getBean(), usersFromGroups, "0", "input");
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
		data = new GroupOwnershipData(MessageLoggerFactory.createMessageLogger(this, true));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		Set<String> usersFromGroups = new HashSet<String>();
		usersFromGroups.add(JCMSSecurityConstants.NASTAVNIK);
		usersFromGroups.add(JCMSSecurityConstants.NOSITELJ);
		if(relativePath==null || relativePath.equals("")) relativePath="0";
		BasicBrowsing.getGroupOwnershipData(data, getCurrentUser().getUserID(), getBean(), usersFromGroups, "0", "update");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
		data.getMessageLogger().registerAsDelayed();
        return SUCCESS;
    }

    public GroupOwnershipData getData() {
		return data;
	}
    public void setData(GroupOwnershipData data) {
		this.data = data;
	}

	public GroupOwnershipBean getBean() {
		return bean;
	}
	public void setBean(GroupOwnershipBean bean) {
		this.bean = bean;
	}
	
	public String getRelativePath() {
		return relativePath;
	}
	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}
}
