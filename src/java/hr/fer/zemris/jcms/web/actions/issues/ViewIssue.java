package hr.fer.zemris.jcms.web.actions.issues;

import com.opensymphony.xwork2.Preparable;

import hr.fer.zemris.jcms.service.IssueTrackingService;
import hr.fer.zemris.jcms.web.actions.ExtendedActionSupport;
import hr.fer.zemris.jcms.web.actions.data.IssuesData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;
import hr.fer.zemris.util.InputStreamWrapper;

@Deprecated
public class ViewIssue extends ExtendedActionSupport implements Preparable{

	private static final long serialVersionUID = 1L;
	
	private String courseInstanceID;
	private Long issueID;
	private InputStreamWrapper streamWrapper;
	private IssuesData data;
	private String delayDate;
		
	public void prepare() throws Exception {
		data = new IssuesData(MessageLoggerFactory.createMessageLogger(this, true));
	}
	
	/**
	 * Dohvat podataka za odabrani issue
	 * @return
	 * @throws Exception
	 */
	public String execute() throws Exception {
     	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
		IssueTrackingService.getIssue(getData(), getIssueID(), getCurrentUser().getUserID());
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)){
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("ITS.noViewPermission"));
			data.getMessageLogger().registerAsDelayed();
			return "list";
		}
		return SUCCESS;
    }
	
	/**
	 * Priprema za novi odgovor dohvat podataka za odabrani issue
	 * @return
	 * @throws Exception
	 */
	public String newAnswer() throws Exception {
     	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	//Dohvat zbog prikaza sadržaja pitanja kod odgovaranja
		IssueTrackingService.getIssue(getData(), getIssueID(), getCurrentUser().getUserID());
		return INPUT;
    }
	
	/**
	 * Slanje odgovora
	 * @return
	 * @throws Exception
	 */
	public String sendAnswer() throws Exception {
     	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	
		if(getData().getAnswerBean().getContent()==null || getData().getAnswerBean().getContent().isEmpty()){
			//Dohvat zbog prikaza sadržaja pitanja kod odgovaranja
			IssueTrackingService.getIssue(getData(), getIssueID(), getCurrentUser().getUserID());
			getData().getMessageLogger().addErrorMessage(getData().getMessageLogger().getText("ITS.contentRequired"));
			getData().getMessageLogger().registerAsDelayed();
			return INPUT;
		}
    	IssueTrackingService.sendAnswer(getData(), getCurrentUser().getUserID(), getIssueID());
    	if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
    		getData().getMessageLogger().addErrorMessage(getData().getMessageLogger().getText("ITS.noReplyPermission"));
			getData().getMessageLogger().registerAsDelayed();
    	}
		return "done";
    }
	
	/**
	 * Odgađanje odgovora na pitanje
	 * @return
	 * @throws Exception
	 */
	public String delayIssue() throws Exception {
     	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	InputStreamWrapper[] wrapper = new InputStreamWrapper[1];
		IssueTrackingService.postponeIssue(getData(), getCurrentUser().getUserID(), getIssueID(), delayDate, wrapper);
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)){
			getData().getMessageLogger().addErrorMessage(getData().getMessageLogger().getText("ITS.noDelayPermission"));
			return "done";
		}
		setStreamWrapper(wrapper[0]);
		return "wrapped-stream";
    }
	
	/**
	 * Poništenje odgode
	 * @return
	 * @throws Exception 
	 */
	public String cancelDelay() throws Exception {
     	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	InputStreamWrapper[] wrapper = new InputStreamWrapper[1];
 		IssueTrackingService.cancelIssueDelay(getData(), getCurrentUser().getUserID(), getIssueID(), wrapper);
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)){
			getData().getMessageLogger().addErrorMessage(getData().getMessageLogger().getText("ITS.noDelayCancelPermission"));
			return "done";
		}
		setStreamWrapper(wrapper[0]);
		return "wrapped-stream";
    }
	
	/**
	 * Inverzija javnosti poruke
	 * @return
	 * @throws Exception
	 */
	public String alterPublicity() throws Exception{
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	InputStreamWrapper[] wrapper = new InputStreamWrapper[1];
		IssueTrackingService.alterIssuePublicity(getData(), getCurrentUser().getUserID(), getIssueID(), getCourseInstanceID(), wrapper);
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)){
			getData().getMessageLogger().addErrorMessage(getData().getMessageLogger().getText("ITS.noPublicityAlterationPermission"));
			getData().getMessageLogger().registerAsDelayed();
			return "done";
		}
		setStreamWrapper(wrapper[0]);
		return "wrapped-stream";
		
	}
	
	/**
	 * Eksplicitno označavanja pitanja kao riješenog tj. prebacivanje u status RESOLVED
	 * @return
	 * @throws Exception
	 */
	public String closeIssue() throws Exception{
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
		IssueTrackingService.closeIssue(getData(), getCurrentUser().getUserID(), getIssueID());
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)){
			
			getData().getMessageLogger().registerAsDelayed();
		}
		return "done";
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

	public Long getIssueID() {
		return issueID;
	}

	public void setIssueID(Long issueID) {
		this.issueID = issueID;
	}

	public void setStreamWrapper(InputStreamWrapper streamWrapper) {
		this.streamWrapper = streamWrapper;
	}

	public InputStreamWrapper getStreamWrapper() {
		return streamWrapper;
	}

	public void setDelayDate(String delayDate) {
		this.delayDate = delayDate;
	}

	public String getDelayDate() {
		return delayDate;
	}
}
