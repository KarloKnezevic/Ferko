package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.UserBean;
import hr.fer.zemris.jcms.parsers.UserImportParser;
import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.UserImportData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserImport extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private UserImportData data = null;
	private String text;
	private Long authTypeID;
	private Set<String> roles = new HashSet<String>();
	
    public String execute() throws Exception {
    	return input();
    }
    
    public String input() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new UserImportData(MessageLoggerFactory.createMessageLogger(this, true));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.getUserImportData(data, getCurrentUser().getUserID(), null, null, null, "input");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
        return INPUT;
    }

    public String importList() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new UserImportData(MessageLoggerFactory.createMessageLogger(this, true));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		List<UserBean> list;
		try {
			list = UserImportParser.parseTabbedFormat(new StringReader(text));
		} catch(Exception ex) {
			data.getMessageLogger().addErrorMessage(ex.getMessage());
			return SHOW_FATAL_MESSAGE;
		}
		BasicBrowsing.getUserImportData(data, getCurrentUser().getUserID(), list, getAuthTypeID(), getRoles(), "importList");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
		data.getMessageLogger().registerAsDelayed();
        return SUCCESS;
    }

    public UserImportData getData() {
		return data;
	}
    public void setData(UserImportData data) {
		this.data = data;
	}

    public void setText(String text) {
		this.text = text;
	}
    public String getText() {
		return this.text;
	}

	public Long getAuthTypeID() {
		return authTypeID;
	}

	public void setAuthTypeID(Long authTypeID) {
		this.authTypeID = authTypeID;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}
}
