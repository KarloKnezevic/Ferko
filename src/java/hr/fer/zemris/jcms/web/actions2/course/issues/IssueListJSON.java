package hr.fer.zemris.jcms.web.actions2.course.issues;

import java.io.StringWriter;
import java.util.List;
import hr.fer.zemris.jcms.beans.IssueBean;
import hr.fer.zemris.jcms.parsers.json.JSONException;
import hr.fer.zemris.jcms.parsers.json.JSONWriter;
import hr.fer.zemris.jcms.service2.course.issues.IssueTrackingService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.IssuesData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.InputStreamWrapper;

@WebClass(dataClass=IssuesData.class)
public class IssueListJSON extends Ext2ActionSupport<IssuesData> {

	private static final long serialVersionUID = 1L;
	private InputStreamWrapper streamWrapper;
	
	/**
	 * Dohvat aktualne liste pitanja
	 * @return
	 * @throws Exception
	 */
	@WebMethodInfo(
		dataResultMappings={
			@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=WRAPPED_STREAM,registerDelayedMessages=false)
		},
		struts2ResultMappings={
			@Struts2ResultMapping(struts2Result=WRAPPED_STREAM)
		}
	)
	public String execute() throws Exception {
		IssueTrackingService.getMessages(getEntityManager(), data);
		StringWriter writer = new StringWriter();
		JSONWriter jw = new JSONWriter(writer).object();
			jw.key("ResultSet").object();
				jw.key("totalResultsAvailable").value(data.getMessageCount());
				jw.key("totalResultsReturned").value(data.getMessageCount());
				jw.key("firstResultPosition").value(1);
				jw.key("Result");
				serializeMessageList(jw, data.getMessageBeans());
			jw.endObject();
		jw.endObject();

		setStreamWrapper(InputStreamWrapper.createInputStreamWrapperFromText(
				writer.toString(), 
				"application/json"));
		return null;
	}
	
	private JSONWriter serializeMessageList(JSONWriter jw, List<IssueBean> issueList) throws JSONException {
		jw.array();
		for(IssueBean b : issueList) {
			jw.object();
			jw.key("msgid").value(b.getID());
			jw.key("title").value(b.getMessageName());
			jw.key("topic").value(b.getTopicName());
			jw.key("owner").value(b.getOwnerName());
			jw.key("creationDate").value(b.getCreationDate());
			jw.key("lastModificationDate").value(b.getLastModificationDate());
			jw.key("status").value(b.getMessageStatus());
			jw.key("public").value(b.getPublicity());
			jw.key("colorIndication").value(b.isColorIndication());
			jw.endObject();
		}
		jw.endArray();
		return jw;
	}
		
	public String getCourseInstanceID() {
		return data.getCourseInstanceID();
	}

	public void setCourseInstanceID(String courseInstanceID) {
		data.setCourseInstanceID(courseInstanceID);
	}

	public void setStreamWrapper(InputStreamWrapper streamWrapper) {
		this.streamWrapper = streamWrapper;
	}

	public InputStreamWrapper getStreamWrapper() {
		return streamWrapper;
	}
	
}
