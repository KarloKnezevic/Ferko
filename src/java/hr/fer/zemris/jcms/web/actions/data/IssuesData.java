package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.beans.IssueAnswerBean;
import hr.fer.zemris.jcms.beans.IssueBean;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.Issue;
import hr.fer.zemris.jcms.model.IssueAnswer;
import hr.fer.zemris.jcms.model.IssueTopic;
import hr.fer.zemris.jcms.service.IssueTrackingService;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.InputStreamWrapper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class IssuesData extends BaseCourseInstance {
	
	private String courseInstanceID;
	private Long topicID;	
	private InputStreamWrapper streamWrapper;
	
	private CourseInstance courseInstance;
	private IssueTopic messageTopic;
	private List<IssueTopic> messageTopics;
	private Issue message;
	private List<IssueBean> messages;
	private List<IssueAnswer> messageAnswers;
	private IssueBean messageBean;
	private IssueAnswerBean answerBean;
	private Long issueID;
	//Ima li trenutni korisnik dozvolu aktivirati/deaktivirati teme
	private boolean canManageTopics;
	//Ima li trenutni korisnik dozvolu postaviti pitanje
	private boolean canCreateIssue;
	//Ima li trenutni korisnik dozvolu poslati odgovor na pitanje
	private boolean canSendAnswer;
	private boolean canDelayAnswer;
	private boolean canChangeIssuePublicity;
	private boolean archive;
	private String issueDeadline = "";
	private boolean canViewStudentsJMBAG;
	private boolean canCloseIssue;
	
	//Fields for V2
	private boolean objectNull;  //general purpose field
	private boolean topicActivity;
	private boolean messageValid;
	private String delayDate;
	private boolean invalidDelayDate;
	private String lastModified;
	private boolean issuePublic;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public IssuesData(IMessageLogger messageLogger) {
		super(messageLogger);
		this.answerBean = new IssueAnswerBean();
		this.messageBean = new IssueBean();
		this.archive = false;
		SimpleDateFormat sdf = new SimpleDateFormat(IssueTrackingService.DATE_FORMAT);
		this.setIssueDeadline(sdf.format(new Date()));
	}
	
	/**
	 * Podaci o primjerku kolegija.
	 * @return primjerak kolegija
	 */
	public CourseInstance getCourseInstance() {
		return courseInstance;
	}

	public void setCourseInstance(CourseInstance courseInstance) {
		this.courseInstance = courseInstance;
	}

	/**
	 * Podaci o temi.
	 * @return tema
	 */
	public IssueTopic getMessageTopic() {
		return messageTopic;
	}

	public void setMessageTopic(IssueTopic messageTopic) {
		this.messageTopic = messageTopic;
	}
	
	/**
	 * Podaci o svim temama na kolegiju.
	 * @return lista tema
	 */
	public List<IssueTopic> getMessageTopics() {
		return messageTopics;
	}

	public void setMessageTopics(List<IssueTopic> messageTopics) {
		this.messageTopics = messageTopics;
	}

	/**
	 * Podaci o poruci.
	 * @return poruka
	 */
	public Issue getMessage() {
		return message;
	}

	public void setMessage(Issue message) {
		this.message = message;
	}

	/**
	 * Podaci porukama koje korisnik moze vidjetu u odredenoj temi.
	 * @return lista poruka
	 */
	public List<IssueBean> getMessageBeans() {
		return messages;
	}

	public void setMessageBeans(List<IssueBean> messages) {
		this.messages = messages;
	}

	public int getMessageCount(){
		if(this.messages==null || this.messages.isEmpty()) return 0;
		else return this.messages.size();
	}
	
	
	/**
	 * Podaci o odgovorima na trenutnu poruku.
	 * @return lista odgovora
	 */
	public List<IssueAnswer> getMessageAnswers() {
		return messageAnswers;
	}

	public void setMessageAnswers(List<IssueAnswer> messageAnswers) {
		this.messageAnswers = messageAnswers;
	}

	public IssueBean getMessageBean() {
		return messageBean;
	}

	public void setMessageBean(IssueBean messageBean) {
		this.messageBean = messageBean;
	}

	public IssueAnswerBean getAnswerBean() {
		return answerBean;
	}

	public void setAnswerBean(IssueAnswerBean answerBean) {
		this.answerBean = answerBean;
	}

	public Long getIssueID() {
		return issueID;
	}

	public void setIssueID(Long issueID) {
		this.issueID = issueID;
	}

	public boolean isCanManageTopics() {
		return canManageTopics;
	}

	public void setCanManageTopics(boolean canManageTopics) {
		this.canManageTopics = canManageTopics;
	}

	public boolean isCanCreateIssue() {
		return canCreateIssue;
	}

	public void setCanCreateIssue(boolean canCreateIssue) {
		this.canCreateIssue = canCreateIssue;
	}

	public boolean getArchive() {
		return archive;
	}

	public void setArchive(boolean archive) {
		this.archive = archive;
	}

	public boolean isCanSendAnswer() {
		return canSendAnswer;
	}

	public void setCanSendAnswer(boolean canSendAnswer) {
		this.canSendAnswer = canSendAnswer;
	}

	public String getIssueDeadline() {
		return issueDeadline;
	}

	public void setIssueDeadline(String issueDeadline) {
		this.issueDeadline = issueDeadline;
	}

	public boolean isCanDelayAnswer() {
		return canDelayAnswer;
	}

	public void setCanDelayAnswer(boolean canDelayAnswer) {
		this.canDelayAnswer = canDelayAnswer;
	}

	public boolean getCanChangeIssuePublicity() {
		return canChangeIssuePublicity;
	}

	public void setCanChangeIssuePublicity(boolean canChangeIssuePublicity) {
		this.canChangeIssuePublicity = canChangeIssuePublicity;
	}

	public void setCanViewStudentsJMBAG(boolean canViewStudentsJMBAG) {
		this.canViewStudentsJMBAG = canViewStudentsJMBAG;
	}

	public boolean getCanViewStudentsJMBAG() {
		return canViewStudentsJMBAG;
	}

	public void setCanCloseIssue(boolean canCloseIssue) {
		this.canCloseIssue = canCloseIssue;
	}

	public boolean isCanCloseIssue() {
		return canCloseIssue;
	}

	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

	public String getCourseInstanceID() {
		return courseInstanceID;
	}

	public void setTopicID(Long topicID) {
		this.topicID = topicID;
	}

	public Long getTopicID() {
		return topicID;
	}

	public void setStreamWrapper(InputStreamWrapper streamWrapper) {
		this.streamWrapper = streamWrapper;
	}

	public InputStreamWrapper getStreamWrapper() {
		return streamWrapper;
	}

	public void setTopicActivity(boolean topicActivity) {
		this.topicActivity = topicActivity;
	}

	public boolean getTopicActivity() {
		return topicActivity;
	}

	public void setObjectNull(boolean objectNull) {
		this.objectNull = objectNull;
	}

	public boolean getObjectNull() {
		return objectNull;
	}

	public void setMessageValid(boolean messageValid) {
		this.messageValid = messageValid;
	}

	public boolean isMessageValid() {
		return messageValid;
	}

	public void setDelayDate(String delayDate) {
		this.delayDate = delayDate;
	}

	public String getDelayDate() {
		return delayDate;
	}

	public void setInvalidDelayDate(boolean invalidDelayDate) {
		this.invalidDelayDate = invalidDelayDate;
	}

	public boolean isInvalidDelayDate() {
		return invalidDelayDate;
	}

	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}

	public String getLastModified() {
		return lastModified;
	}

	public void setIssuePublic(boolean issuePublic) {
		this.issuePublic = issuePublic;
	}

	public boolean isIssuePublic() {
		return issuePublic;
	}

}
