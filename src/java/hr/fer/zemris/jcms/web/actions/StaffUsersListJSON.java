package hr.fer.zemris.jcms.web.actions;

import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.jcms.beans.StringNameStringValue;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.StaffUsersListJSONData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;
import hr.fer.zemris.util.InputStreamWrapper;
import hr.fer.zemris.util.JSONFormatter;

public class StaffUsersListJSON extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;
	
	private StaffUsersListJSONData data = null;
	private String q;
	private InputStreamWrapper streamWrapper;
	
    public String execute() throws Exception {
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new StaffUsersListJSONData(MessageLoggerFactory.createMessageLogger(this, false));

		List<StringNameStringValue> list;
		
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) {
    		list = new ArrayList<StringNameStringValue>();
    		streamWrapper = InputStreamWrapper.createInputStreamWrapperFromText(
    				JSONFormatter.getJSONKeyValue(list, JSONFormatter.SortOrder.BY_VALUE, true), 
    				"application/json");
    		return WRAPPED_STREAM;
    	}

		// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.getStaffUsersListJSONData(data, getCurrentUser().getUserID(), getQ());
		list = new ArrayList<StringNameStringValue>();
		for(User user : data.getUsers()) {
			list.add(new StringNameStringValue(
				user.getId().toString(),
				user.getLastName()+", "+user.getFirstName() + " ("+user.getJmbag()+")"
			));
		}
		streamWrapper = InputStreamWrapper.createInputStreamWrapperFromText(
				JSONFormatter.getJSONKeyValue(list, JSONFormatter.SortOrder.BY_VALUE, false), 
				"application/json");
		return WRAPPED_STREAM;
    }

    public StaffUsersListJSONData getData() {
		return data;
	}
    public void setData(StaffUsersListJSONData data) {
		this.data = data;
	}
    
    public String getQ() {
		return q;
	}
    public void setQ(String q) {
		this.q = q;
	}
    
    public InputStreamWrapper getStreamWrapper() {
		return streamWrapper;
	}
}
