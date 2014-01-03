package hr.fer.zemris.jcms.beans;

import hr.fer.zemris.jcms.model.Issue;
import hr.fer.zemris.jcms.model.IssueAssistantVersion;
import hr.fer.zemris.jcms.model.extra.IssueStatus;
import hr.fer.zemris.jcms.service.IssueTrackingService;
import hr.fer.zemris.jcms.web.actions.data.IssuesData;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Pitanje/poruka koje šalje student
 * @author IvanFer
 */
public class IssueBean {

	private Long ID;
	private String messageName;
	private String messageContent;
	private Long topicID;
	private String topicName;
	private boolean declaredPublic;
	private String ownerName;
	private String creationDate;
	private String lastModificationDate;
	private String delayDate;
	private List<IssueAnswerBean> answers;
	private String messageStatus;
	private String publicity;
	private boolean colorIndication;
	private String currentTime;
	private boolean isDelayed;
	private boolean offerExpliciteClosure;
	
	public IssueBean(){
		
	}
	
	public IssueBean(Issue m, IssuesData data, Long userID){
		setID(m.getId());
		setMessageContent(m.getContent().trim());
		setMessageName(m.getName().trim());
		setDeclaredPublic(m.isDeclaredPublic());
		if(data.getCanViewStudentsJMBAG()){
			setOwnerName(m.getUser().getLastName()+", "+m.getUser().getFirstName() + " (" + m.getUser().getJmbag()+")");
		}else{
			setOwnerName(m.getUser().getLastName()+", "+m.getUser().getFirstName());
		}
		setTopicName(m.getTopic().getName().trim());
		SimpleDateFormat sdf = new SimpleDateFormat(IssueTrackingService.DATE_FORMAT);
		setCreationDate(sdf.format(m.getCreationDate()));
		setDelayDate((m.getDelayDate()==null)? data.getMessageLogger().getText("ITS.messageNotPostponed") : sdf.format(m.getDelayDate()));
		setIsDelayed((m.getDelayDate()==null)? false : true);
		setLastModificationDate(sdf.format(m.getLastModificationDate()));
		if(m.getDelayDate()!=null) setDelayDate(sdf.format(m.getDelayDate()));
		setTopicName(m.getTopic().getName());
		switch(m.getStatus()){
			case NEW : setMessageStatus(data.getMessageLogger().getText("ITS.messageStatusNew"));break;
			case READ : setMessageStatus(data.getMessageLogger().getText("ITS.messageStatusRead"));break;
			case POSTPONED : setMessageStatus(data.getMessageLogger().getText("ITS.messageStatusPostponed"));break;
			case RESOLVED : setMessageStatus(data.getMessageLogger().getText("ITS.messageStatusResolved"));break;
			case UNREAD : setMessageStatus(data.getMessageLogger().getText("ITS.messageStatusUnread"));break;
		}
		setPublicity(m.isDeclaredPublic()?data.getMessageLogger().getText("ITS.issueIsPublic") : data.getMessageLogger().getText("ITS.issueNotPublic"));
		setColorIndication(false);
		if(data.isCanCreateIssue()){ //Ako je student
			if(m.getModificationVersion() > m.getStudentVersion()) setColorIndication(true);
		}else{ //Ako je asistent
			if(checkVersions(m, userID)) setColorIndication(true);
			else setColorIndication(false);
		}
		setCurrentTime(sdf.format(new Date()));
		setOfferExpliciteClosure(m.getStatus().equals(IssueStatus.UNREAD)? true : false);
	}
	
	private boolean checkVersions(Issue msg, Long userID){
		for(IssueAssistantVersion mav : msg.getVersions()){
			if(mav.getUser().getId().equals(userID)){
				if(msg.getModificationVersion() > mav.getIssueVersion()) return true;
				break;
			}
		}
		return false;
	}
	
	
	public Long getID() {
		return ID;
	}

	public void setID(Long id) {
		this.ID = id;
	}

	public String getMessageName() {
		return messageName;
	}

	public void setMessageName(String messageName) {
		this.messageName = messageName;
	}

	public Long getTopicID() {
		return topicID;
	}

	public void setTopicID(Long topicID) {
		this.topicID = topicID;
	}

	public boolean getDeclaredPublic() {
		return declaredPublic;
	}

	public void setDeclaredPublic(boolean declaredPublic) {
		this.declaredPublic = declaredPublic;
	}

	
	public String getMessageContent() {
		return messageContent;
	}

	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}

	
	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerUserName) {
		this.ownerName = ownerUserName;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	public List<IssueAnswerBean> getAnswers() {
		return answers;
	}

	public void setAnswers(List<IssueAnswerBean> answers) {
		this.answers = answers;
	}

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public String getMessageStatus() {
		return messageStatus;
	}

	public void setMessageStatus(String messageStatus) {
		this.messageStatus = messageStatus;
	}

	public String getPublicity() {
		return publicity;
	}
	public void setPublicity(String publicity) {
		this.publicity = publicity;
	}

	
	public String getDelayDate() {
		return delayDate;
	}

	public void setDelayDate(String delayDate) {
		this.delayDate = delayDate;
	}

	
	public boolean isColorIndication() {
		return colorIndication;
	}

	public void setColorIndication(boolean colorIndication) {
		this.colorIndication = colorIndication;
	}
	
	public String getLastModificationDate() {
		return lastModificationDate;
	}

	public void setLastModificationDate(String lastModificationDate) {
		this.lastModificationDate = lastModificationDate;
	}

	public void setCurrentTime(String currentTime) {
		this.currentTime = currentTime;
	}

	public String getCurrentTime() {
		return currentTime;
	}
	
	public void setIsDelayed(boolean isDelayed) {
		this.isDelayed = isDelayed;
	}

	public boolean getIsDelayed() {
		return isDelayed;
	}

	public void setOfferExpliciteClosure(boolean offerExpliciteClosure) {
		this.offerExpliciteClosure = offerExpliciteClosure;
	}

	public boolean isOfferExpliciteClosure() {
		return offerExpliciteClosure;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IssueBean other = (IssueBean) obj;
		if (other.getMessageName().equals(this.getMessageName()) && 
				other.getMessageContent().equals(this.getMessageContent()) &&
					other.getTopicID().equals(this.getTopicID()))
		{
			return true;
		}else{
			return false;	
		}
		
	}


}
