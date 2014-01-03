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

@WebClass(dataClass=IssuesData.class)
public class NewIssue extends Ext2ActionSupport<IssuesData> {

	private static final long serialVersionUID = 1L;
	
	public static final String MESSAGE_INVALID = "message_invalid";
	
	/**
	 * Priprema za postavljanje novog pitanja
	 * @return
	 * @throws Exception
	 */
	@WebMethodInfo(		
		dataResultMappings={
			@DataResultMapping(dataResult=AbstractActionData.RESULT_FATAL,struts2Result=SUCCESS,registerDelayedMessages=true),
			@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=INPUT,registerDelayedMessages=false)
		},
		struts2ResultMappings={@Struts2ResultMapping(struts2Result=INPUT,navigBuilder=IssuesBuilder.class, navigBuilderIsRoot=false, additionalMenuItems={"m2","ITS.newIssueForm"})}
	)
	public String execute() throws Exception {
		IssueTrackingService.getTopics(getEntityManager(), data, "ACTIVE_TOPICS_ONLY");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)){
			return null;
    	}
		return null;
    }
	
	/**
	 * Slanje novog pitanja 
	 * @return
	 * @throws Exception
	 */
	@WebMethodInfo(		
			dataResultMappings={
				@DataResultMapping(dataResult=AbstractActionData.RESULT_FATAL,struts2Result=SUCCESS,registerDelayedMessages=true),
				@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=SUCCESS,registerDelayedMessages=true),
				@DataResultMapping(dataResult=NewIssue.MESSAGE_INVALID,struts2Result=INPUT,registerDelayedMessages=true)
			},
			struts2ResultMappings={@Struts2ResultMapping(struts2Result=INPUT,navigBuilder=IssuesBuilder.class, navigBuilderIsRoot=false, additionalMenuItems={"m2","ITS.newIssueForm"}),
								   @Struts2ResultMapping(struts2Result=SUCCESS,navigBuilder=IssuesBuilder.class, navigBuilderIsRoot=true)}
		)
	public String newIssueAdd() throws Exception{
    	IssueTrackingService.validateNewMessage(getEntityManager(), data);
    	if(data.getResult().equals(MESSAGE_INVALID)) return null;
    	
    	//Ako su podaci OK
    	IssueTrackingService.sendMessage(getEntityManager(), data);
    	return null;
	}
	
	public String getCourseInstanceID() {
		return data.getCourseInstanceID();
	}

	public void setCourseInstanceID(String courseInstanceID) {
		data.setCourseInstanceID(courseInstanceID);
	}

	 
	
}
