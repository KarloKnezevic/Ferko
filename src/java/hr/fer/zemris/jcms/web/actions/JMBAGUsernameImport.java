package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.ext.JMBAGLoginBean;
import hr.fer.zemris.jcms.parsers.JMBAGUsernameParser;
import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.JMBAGUsernameImportData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import java.io.StringReader;
import java.util.List;

public class JMBAGUsernameImport extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private JMBAGUsernameImportData data = null;
	private String text;
	
    public String execute() throws Exception {
    	return input();
    }
    
    public String input() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new JMBAGUsernameImportData(MessageLoggerFactory.createMessageLogger(this, true));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.getJMBAGUsernameImportData(data, getCurrentUser().getUserID(), null, "input");
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
		data = new JMBAGUsernameImportData(MessageLoggerFactory.createMessageLogger(this, true));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		List<JMBAGLoginBean> list;
		try {
			list = JMBAGUsernameParser.parseTabbedFormat(new StringReader(text));
		} catch(Exception ex) {
			data.getMessageLogger().addErrorMessage(ex.getMessage());
			return SHOW_FATAL_MESSAGE;
		}
		BasicBrowsing.getJMBAGUsernameImportData(data, getCurrentUser().getUserID(), list, "importList");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
		data.getMessageLogger().registerAsDelayed();
        return SUCCESS;
    }

    public JMBAGUsernameImportData getData() {
		return data;
	}
    public void setData(JMBAGUsernameImportData data) {
		this.data = data;
	}

    public void setText(String text) {
		this.text = text;
	}
    public String getText() {
		return this.text;
	}
}
