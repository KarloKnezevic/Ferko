package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.GoData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

/**
 * @author marcupic
 *
 * Ovo je akcija koju moze pokrenuti samo administrator sustava, a sluzi tome
 * da u bazi stvori kontekste za podrzane tipov dogadaja, kako bi se eventi u 
 * kalendaru mogu uzivo klikati i bojati. 
 */
public class InitEvents extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private String semesterID;
	private GoData data;
	
    public String execute() throws Exception {
    	if(!hasCurrentUser()) return NOT_LOGGED_IN;
    	data = new GoData(MessageLoggerFactory.createMessageLogger(this,true));
		BasicBrowsing.getInitEventsData(data, getCurrentUser().getUserID(),semesterID);
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return AbstractActionData.RESULT_FATAL;
		}
		data.getMessageLogger().registerAsDelayed();
        return AbstractActionData.RESULT_SUCCESS;
    }

	public GoData getData() {
		return data;
	}
    public void setData(GoData data) {
		this.data = data;
	}
    
    public String getSemesterID() {
		return semesterID;
	}
    
    public void setSemesterID(String semesterID) {
		this.semesterID = semesterID;
	}
}
