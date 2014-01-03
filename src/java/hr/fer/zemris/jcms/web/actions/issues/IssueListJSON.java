package hr.fer.zemris.jcms.web.actions.issues;

import com.opensymphony.xwork2.Preparable;

import hr.fer.zemris.jcms.service.IssueTrackingService;
import hr.fer.zemris.jcms.web.actions.ExtendedActionSupport;
import hr.fer.zemris.jcms.web.actions.data.IssuesData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

@Deprecated
public class IssueListJSON extends ExtendedActionSupport implements Preparable{

	private static final long serialVersionUID = 1L;
	
	private String courseInstanceID;
	private Long messageTopicID;
	private Long messageID;
	 
	private IssuesData data;
		
	public void prepare() throws Exception {
		data = new IssuesData(MessageLoggerFactory.createMessageLogger(this, true));
	}
	
	/**
	 * Dohvat aktualne liste pitanja
	 * @return
	 * @throws Exception
	 */
	public String execute() throws Exception {
    	if(getCourseInstanceID()==null || getCourseInstanceID().equals("")) {
    		// Ako nije zadan courseInstanceID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check; 
		IssueTrackingService.getMessages(getData(), getCurrentUser().getUserID(), getCourseInstanceID());
		return SUCCESS;
	}
	
	public String getCourseInstanceID() {
		return courseInstanceID;
	}

	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

	public Long getMessageTopicID() {
		return messageTopicID;
	}

	public void setMessageTopicID(Long messageTopicID) {
		this.messageTopicID = messageTopicID;
	}

	public Long getMessageID() {
		return messageID;
	}

	public void setMessageID(Long messageID) {
		this.messageID = messageID;
	}

	public IssuesData getData() {
		return data;
	}

	public void setData(IssuesData data) {
		this.data = data;
	}
	  
	 
	
}
