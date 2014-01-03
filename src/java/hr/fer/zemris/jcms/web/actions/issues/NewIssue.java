package hr.fer.zemris.jcms.web.actions.issues;

import com.opensymphony.xwork2.Preparable;

import hr.fer.zemris.jcms.service.IssueTrackingService;
import hr.fer.zemris.jcms.web.actions.ExtendedActionSupport;
import hr.fer.zemris.jcms.web.actions.data.IssuesData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

@Deprecated
public class NewIssue extends ExtendedActionSupport implements Preparable{

	private static final long serialVersionUID = 1L;
	
	private String courseInstanceID;
	private IssuesData data;
		
	public void prepare() throws Exception {
		data = new IssuesData(MessageLoggerFactory.createMessageLogger(this, true));
	}
	
	/**
	 * Priprema za postavljanje novog pitanja
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
		IssueTrackingService.getTopics(data, getCurrentUser().getUserID(), courseInstanceID, "ACTIVE_TOPICS_ONLY");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)){
    		getData().getMessageLogger().addErrorMessage(getData().getMessageLogger().getText("ITS.noPermissionToCreateIssueOnCourse"));
			getData().getMessageLogger().registerAsDelayed();
			return SUCCESS;
    	}
		return INPUT;
    }
	
	/**
	 * Slanje novog pitanja 
	 * @return
	 * @throws Exception
	 */
	public String newIssueAdd() throws Exception{
    	if(getCourseInstanceID()==null || getCourseInstanceID().equals("")) {
    		// Ako nije zadan courseInstanceID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	if(!IssueTrackingService.validateNewMessage(data)){
    		IssueTrackingService.getTopics(data, getCurrentUser().getUserID(), courseInstanceID, "ACTIVE_TOPICS_ONLY");
    		data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("ITS.messageNotValid"));
    		data.getMessageLogger().registerAsDelayed();
    		return INPUT;
    	}
    	//Ako su podaci OK
    	IssueTrackingService.sendMessage(data, courseInstanceID, getCurrentUser().getUserID());
    	if(data.getResult().equals(AbstractActionData.RESULT_FATAL)){
    		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("ITS.noPermissionToCreateIssueOnCourse"));
    	}else{
    	   	data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("ITS.messageSuccessfullySent"));
    	}
    	data.getMessageLogger().registerAsDelayed();
    	return SUCCESS;
	}
	
	public String getCourseInstanceID() {
		return courseInstanceID;
	}

	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

	public IssuesData getData() {
		return data;
	}

	public void setData(IssuesData data) {
		this.data = data;
	}
	  
	 
	
}
