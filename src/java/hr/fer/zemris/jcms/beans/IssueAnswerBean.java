package hr.fer.zemris.jcms.beans;

import java.text.SimpleDateFormat;

import hr.fer.zemris.jcms.model.IssueAnswer;
import hr.fer.zemris.jcms.service.IssueTrackingService;


/**
 * Pitanje/poruka koje šalje student
 * @author IvanFer
 */
public class IssueAnswerBean {

	private Long ID;
	private String content;
	private String user;
	private String date;
	
	public IssueAnswerBean(){
		
	}
	
	public IssueAnswerBean(IssueAnswer answer){
		this.ID = answer.getId();
		this.content = answer.getContent();
		this.user = answer.getUser().getFirstName()+" "+answer.getUser().getLastName();
		SimpleDateFormat sdf = new SimpleDateFormat(IssueTrackingService.DATE_FORMAT);
		this.date = sdf.format(answer.getDate());
	}
	
	public Long getID() {
		return ID;
	}

	public void setID(Long id) {
		this.ID = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String messageContent) {
		this.content = messageContent;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}



	
}
