package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.GoData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

public class Go extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private String eid;
	private GoData data;
	
    public String execute() throws Exception {
    	if(!hasCurrentUser()) return NOT_LOGGED_IN;
    	data = new GoData(MessageLoggerFactory.createMessageLogger(this,true));
		BasicBrowsing.getGoData(data, getCurrentUser().getUserID(),eid);
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return AbstractActionData.RESULT_FATAL;
		}
        return data.getResult();
    }

	public GoData getData() {
		return data;
	}
    public void setData(GoData data) {
		this.data = data;
	}

    public String getEid() {
		return eid;
	}
    
    public void setEid(String eid) {
		this.eid = eid;
	}
}
