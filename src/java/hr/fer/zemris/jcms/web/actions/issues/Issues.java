package hr.fer.zemris.jcms.web.actions.issues;

import com.opensymphony.xwork2.Preparable;
import hr.fer.zemris.jcms.service.IssueTrackingService;
import hr.fer.zemris.jcms.web.actions.ExtendedActionSupport;
import hr.fer.zemris.jcms.web.actions.data.IssuesData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;
import hr.fer.zemris.util.InputStreamWrapper;
 
@Deprecated
public class Issues extends ExtendedActionSupport implements Preparable{

	private static final long serialVersionUID = 1L;
	
	private String courseInstanceID;
	private IssuesData data;
	private InputStreamWrapper streamWrapper;
	private Long topicID;
		
	public void prepare() throws Exception {
		data = new IssuesData(MessageLoggerFactory.createMessageLogger(this, true));
	}
	
	/**
	 * Dohvat aktualne liste pitanja
	 * Dohvat se vrši implicitno putem skripte u stranici i IssueListJSON akcije
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
    	IssueTrackingService.setPermissionsExt(getData(), getCurrentUser().getUserID(), getCourseInstanceID());
    	return SUCCESS;
    }
	
	/**
	 * Priprema za uređivanje aktivnosti tema
	 * @return
	 * @throws Exception
	 */
	public String editTopics() throws Exception{
     	if(getCourseInstanceID()==null || getCourseInstanceID().equals("")) {
    		// Ako nije zadan courseInstanceID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	IssueTrackingService.getTopics(getData(), getCurrentUser().getUserID(), getCourseInstanceID(), "ALL_TOPICS");
    	if(data.getResult().equals(AbstractActionData.RESULT_FATAL)){
    		getData().getMessageLogger().addErrorMessage(getData().getMessageLogger().getText("ITS.noTopicManagementPermission"));
			getData().getMessageLogger().registerAsDelayed();
			return SUCCESS;
    	}
		return "topics";
	}
	
	/**
	 * Pohrana aktivnosti tema
	 * @return
	 * @throws Exception
	 */
	public String updateTopics() throws Exception{
     	if(getCourseInstanceID()==null || getCourseInstanceID().equals("")) {
    		// Ako nije zadan courseInstanceID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	InputStreamWrapper[] wrapper = new InputStreamWrapper[1];
    	IssueTrackingService.updateMessageTopicsActivity(getData(), getCurrentUser().getUserID(), getCourseInstanceID(), getTopicID(), wrapper);
    	if(data.getResult().equals(AbstractActionData.RESULT_FATAL)){
    		getData().getMessageLogger().addErrorMessage(getData().getMessageLogger().getText("ITS.noTopicManagementPermission"));
			getData().getMessageLogger().registerAsDelayed();
			return SUCCESS;
    	}
		setStreamWrapper(wrapper[0]);
		return "wrapped-stream";
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

	public void setStreamWrapper(InputStreamWrapper streamWrapper) {
		this.streamWrapper = streamWrapper;
	}

	public InputStreamWrapper getStreamWrapper() {
		return streamWrapper;
	}

	public void setTopicID(Long topicID) {
		this.topicID = topicID;
	}

	public Long getTopicID() {
		return topicID;
	}
}
