package hr.fer.zemris.jcms.web.actions2;

import java.io.IOException;
import java.io.StringWriter;

import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.parsers.json.JSONException;
import hr.fer.zemris.jcms.parsers.json.JSONWriter;
import hr.fer.zemris.jcms.service2.UserFetcherService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.UserFetcherData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.LoggerMessage;
import hr.fer.zemris.jcms.web.navig.builders.BuilderDefault;
import hr.fer.zemris.util.InputStreamWrapper;

@WebClass(dataClass=UserFetcherData.class, defaultNavigBuilder=BuilderDefault.class)
public class UserFetcher extends Ext2ActionSupport<UserFetcherData> {

	private static final long serialVersionUID = 1L;
	private InputStreamWrapper streamWrapper;

	@WebMethodInfo(
			dataResultMappings={
				@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=WRAPPED_STREAM,registerDelayedMessages=false),
				@DataResultMapping(dataResult=AbstractActionData.RESULT_FATAL,struts2Result=WRAPPED_STREAM,registerDelayedMessages=false)
			},
			struts2ResultMappings={
				@Struts2ResultMapping(struts2Result=WRAPPED_STREAM, navigBuilder=BuilderDefault.class)
			}
		)
	public String execute() throws IOException, JSONException {
		UserFetcherService.fetchUsers(getEntityManager(), data);
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			StringWriter writer = new StringWriter();
			JSONWriter jw = new JSONWriter(writer).object();
			jw.key("status").value("ERR");
			jw.key("msgs").array();
			for(LoggerMessage lm : getData().getMessageLogger().getMessages()) {
				jw.value(lm.getMessageText());
			}
			jw.endArray();
			jw.endObject();
			streamWrapper = InputStreamWrapper.createInputStreamWrapperFromText(
					writer.toString(), 
    				"application/json");
    		return null;
		}
		StringWriter writer = new StringWriter();
		JSONWriter jw = new JSONWriter(writer).object();
		jw.key("status").value("OK");
		jw.key("users").array();
		for(User user : data.getUsers()) {
			jw.object();
			jw.key("i").value(user.getId());
			jw.key("j").value(user.getJmbag());
			jw.key("f").value(user.getFirstName());
			jw.key("l").value(user.getLastName());
			jw.endObject();
		}
		jw.endArray();
		jw.endObject();
		streamWrapper = InputStreamWrapper.createInputStreamWrapperFromText(
				writer.toString(), 
				"application/json");
		return null;
	}
	
	public InputStreamWrapper getStreamWrapper() {
		return streamWrapper;
	}
	public void setStreamWrapper(InputStreamWrapper streamWrapper) {
		this.streamWrapper = streamWrapper;
	}
}
