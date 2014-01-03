package hr.fer.zemris.jcms.web.actions2.course.issues;

import hr.fer.zemris.jcms.service2.course.issues.IssueTrackingService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.IssuesData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.builders.course.issues.IssuesBuilder;
import hr.fer.zemris.util.InputStreamWrapper;

@WebClass(dataClass=IssuesData.class)
public class ViewIssue extends Ext2ActionSupport<IssuesData>{

	private static final long serialVersionUID = 1L;
	
	public static final String ISSUE_LIST = "list";
	public static final String INVALID_ANSWER = "invalid_answer";
	public static final String ANSWER_SUCCESS = "done";
	
	private InputStreamWrapper streamWrapper;
	
	/**
	 * Dohvat podataka za odabrani issue
	 * @return
	 * @throws Exception
	 */
	@WebMethodInfo(
			dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=SUCCESS),
								@DataResultMapping(dataResult=AbstractActionData.RESULT_FATAL,struts2Result=ISSUE_LIST, registerDelayedMessages=true)},
			struts2ResultMappings={@Struts2ResultMapping(struts2Result=ISSUE_LIST,navigBuilder=IssuesBuilder.class, navigBuilderIsRoot=true),
								   @Struts2ResultMapping(struts2Result=SUCCESS,navigBuilder=IssuesBuilder.class, navigBuilderIsRoot=true)})
	public String execute() throws Exception {

		IssueTrackingService.getIssue(getEntityManager(), data);
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) return null;
		return null;
    }
	
	/**
	 * Priprema za novi odgovor dohvat podataka za odabrani issue
	 * @return
	 * @throws Exception
	 */
	@WebMethodInfo(
		dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=INPUT),
							@DataResultMapping(dataResult=AbstractActionData.RESULT_FATAL,struts2Result=ISSUE_LIST, registerDelayedMessages=true)},
		struts2ResultMappings={@Struts2ResultMapping(struts2Result=ISSUE_LIST,navigBuilder=IssuesBuilder.class, navigBuilderIsRoot=true),
							   @Struts2ResultMapping(struts2Result=INPUT,navigBuilder=IssuesBuilder.class, navigBuilderIsRoot=true)})
	public String newAnswer() throws Exception {
    	//Dohvat zbog prikaza sadržaja pitanja kod odgovaranja
		IssueTrackingService.getIssue(getEntityManager(), data);
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) return null;
		return null;
    }
	
	/**
	 * Slanje odgovora
	 * @return
	 * @throws Exception
	 */
	@WebMethodInfo(
			dataResultMappings={@DataResultMapping(dataResult=ViewIssue.INVALID_ANSWER,struts2Result=INPUT, registerDelayedMessages=true),
								@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=ViewIssue.ANSWER_SUCCESS, registerDelayedMessages=true),
								@DataResultMapping(dataResult=AbstractActionData.RESULT_FATAL,struts2Result=ISSUE_LIST, registerDelayedMessages=true)},
			struts2ResultMappings={@Struts2ResultMapping(struts2Result=ISSUE_LIST,navigBuilder=IssuesBuilder.class, navigBuilderIsRoot=true),
								   @Struts2ResultMapping(struts2Result=INPUT,navigBuilder=IssuesBuilder.class, navigBuilderIsRoot=true),
								   @Struts2ResultMapping(struts2Result=ANSWER_SUCCESS,navigBuilder=IssuesBuilder.class, navigBuilderIsRoot=true)})
	public String sendAnswer() throws Exception {
    	IssueTrackingService.sendAnswer(getEntityManager(), data);
    	if(data.getResult().equals(INVALID_ANSWER)) return null;
		return execute();
    }
	
	/**
	 * Odgađanje odgovora na pitanje
	 * @return
	 * @throws Exception
	 */
	@WebMethodInfo(
			dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=WRAPPED_STREAM, registerDelayedMessages=true),
								@DataResultMapping(dataResult=AbstractActionData.RESULT_FATAL,struts2Result=ViewIssue.ANSWER_SUCCESS, registerDelayedMessages=true)},
			struts2ResultMappings={@Struts2ResultMapping(struts2Result=WRAPPED_STREAM),
								   @Struts2ResultMapping(struts2Result=ANSWER_SUCCESS,navigBuilder=IssuesBuilder.class, navigBuilderIsRoot=true)})
	public String delayIssue() throws Exception {   
		IssueTrackingService.postponeIssue(getEntityManager(), data);
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) return null;		
		String result;
		if(data.isInvalidDelayDate()){
			result = "<code>0</code>";
		}else{
			result = "<code>1</code><newstatus>Odgođeno</newstatus><lastmodified>" + data.getLastModified() + "</lastmodified>";
		}
		String text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><result>" + result + "</result>";
		streamWrapper = InputStreamWrapper.createInputStreamWrapperFromText(text);

		return null;
    }
	
	/**
	 * Poništenje odgode
	 * @return
	 * @throws Exception 
	 */
	@WebMethodInfo(
			dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=WRAPPED_STREAM, registerDelayedMessages=true),
								@DataResultMapping(dataResult=AbstractActionData.RESULT_FATAL,struts2Result=ViewIssue.ANSWER_SUCCESS, registerDelayedMessages=true)},
			struts2ResultMappings={@Struts2ResultMapping(struts2Result=WRAPPED_STREAM),
								   @Struts2ResultMapping(struts2Result=ANSWER_SUCCESS,navigBuilder=IssuesBuilder.class, navigBuilderIsRoot=true)})
	public String cancelDelay() throws Exception {
 		IssueTrackingService.cancelIssueDelay(getEntityManager(), data);
 		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) return null;
		String result = "<code>1</code><newstatus>"+"Pročitano"+"</newstatus><lastmodified>"+data.getLastModified()+"</lastmodified>";
		String text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><result>" + result + "</result>";
		streamWrapper = InputStreamWrapper.createInputStreamWrapperFromText(text);		
		return null;
    }
	
	/**
	 * Inverzija javnosti poruke
	 * @return
	 * @throws Exception
	 */
	@WebMethodInfo(
			dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=WRAPPED_STREAM, registerDelayedMessages=true),
								@DataResultMapping(dataResult=AbstractActionData.RESULT_FATAL,struts2Result=ViewIssue.ANSWER_SUCCESS, registerDelayedMessages=true)},
			struts2ResultMappings={@Struts2ResultMapping(struts2Result=WRAPPED_STREAM),
								   @Struts2ResultMapping(struts2Result=ANSWER_SUCCESS,navigBuilder=IssuesBuilder.class, navigBuilderIsRoot=true)})
	public String alterPublicity() throws Exception{
		IssueTrackingService.alterIssuePublicity(getEntityManager(), data);
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) return null;
		String result;
		if(data.isIssuePublic()) result = "<code>1</code>";
		else result = "<code>0</code>" ;
		String text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><result>" + result + "</result>";
		streamWrapper = InputStreamWrapper.createInputStreamWrapperFromText(text);		
		return null;		
	}
	
	/**
	 * Eksplicitno označavanja pitanja kao riješenog tj. prebacivanje u status RESOLVED
	 * @return
	 * @throws Exception
	 */
	@WebMethodInfo(
			dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=ViewIssue.ANSWER_SUCCESS, registerDelayedMessages=true),
								@DataResultMapping(dataResult=AbstractActionData.RESULT_FATAL,struts2Result=ViewIssue.ANSWER_SUCCESS, registerDelayedMessages=true)},
			struts2ResultMappings={@Struts2ResultMapping(struts2Result=ANSWER_SUCCESS,navigBuilder=IssuesBuilder.class, navigBuilderIsRoot=true)})
	public String closeIssue() throws Exception{
		IssueTrackingService.closeIssue(getEntityManager(), data);
		return null;
	}
	
	public String getCourseInstanceID() {
		return data.getCourseInstanceID();
	}

	public void setCourseInstanceID(String courseInstanceID) {
		data.setCourseInstanceID(courseInstanceID);
	}

	public IssuesData getData() {
		return data;
	}

	public void setData(IssuesData data) {
		this.data = data;
	}

	public Long getIssueID() {
		return data.getIssueID();
	}

	public void setIssueID(Long issueID) {
		data.setIssueID(issueID);
	}

	public void setStreamWrapper(InputStreamWrapper streamWrapper) {
		this.streamWrapper = streamWrapper;
	}

	public InputStreamWrapper getStreamWrapper() {
		return streamWrapper;
	}

	public void setDelayDate(String delayDate) {
		data.setDelayDate(delayDate);
	}

	public String getDelayDate() {
		return data.getDelayDate();
	}
}
