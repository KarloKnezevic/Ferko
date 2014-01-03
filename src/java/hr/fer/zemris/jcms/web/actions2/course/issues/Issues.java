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
public class Issues extends Ext2ActionSupport<IssuesData>{

	private static final long serialVersionUID = 2L;
	
	private static final String TOPIC_VIEW = "topics";
	
	private InputStreamWrapper streamWrapper;
	
	
	/**
	 * Dohvat aktualne liste pitanja
	 * Dohvat se vrši implicitno putem skripte u stranici i IssueListJSON akcije
	 * @return
	 * @throws Exception
	 */
	@WebMethodInfo
	public String execute() throws Exception {
    	IssueTrackingService.setPermissionsExt(getEntityManager(), data);
    	return null;
    }
	
	/**
	 * Priprema za uređivanje aktivnosti tema
	 * @return
	 * @throws Exception
	 */
	@WebMethodInfo(dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=TOPIC_VIEW)},
	struts2ResultMappings={@Struts2ResultMapping(struts2Result=TOPIC_VIEW,navigBuilder=IssuesBuilder.class, navigBuilderIsRoot=false, additionalMenuItems={"m2","ITS.topicManagementSubtitle"})})
	public String editTopics() throws Exception{
    	IssueTrackingService.getTopics(getEntityManager(), data, "ALL_TOPICS");
    	if(data.getResult().equals(AbstractActionData.RESULT_FATAL)){
			return execute();
    	}
		return null;
	}
	
	/**
	 * Pohrana aktivnosti tema
	 * @return
	 * @throws Exception
	 */
	@WebMethodInfo(		
		dataResultMappings={
			@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=WRAPPED_STREAM,registerDelayedMessages=false),
			@DataResultMapping(dataResult=AbstractActionData.RESULT_FATAL,struts2Result=WRAPPED_STREAM,registerDelayedMessages=true)
		},
		struts2ResultMappings={
			@Struts2ResultMapping(struts2Result=WRAPPED_STREAM)
		}
	)
	public String updateTopics() throws Exception{
    	IssueTrackingService.updateMessageTopicsActivity(getEntityManager(), data);
    	if(data.getResult().equals(AbstractActionData.RESULT_FATAL)){
			return execute();
    	}else{
    		String param;
    		if(!data.getObjectNull()){
    			if(data.getTopicActivity()) param="<code>1</code><present>1</present>";
    			else param="<code>1</code><present>0</present>";
    		}else{
    			param="<code>-1</code>";
    		}
    		String text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><result>" + param + "</result>";
    		streamWrapper = InputStreamWrapper.createInputStreamWrapperFromText(text);
    	}
		return null;
	}
	
	public String getCourseInstanceID() {
		return data.getCourseInstanceID();
	}
	public void setCourseInstanceID(String courseInstanceID) {
		data.setCourseInstanceID(courseInstanceID);
	}

	public void setStreamWrapper(InputStreamWrapper streamWrapper) {
		this.streamWrapper=streamWrapper;
	}

	public InputStreamWrapper getStreamWrapper() {
		return this.streamWrapper;
	}

	public void setTopicID(Long topicID) {
		data.setTopicID(topicID);
	}

	public Long getTopicID() {
		return data.getTopicID();
	}
}
