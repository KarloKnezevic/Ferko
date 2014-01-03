package hr.fer.zemris.jcms.web.actions2.course.groups;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import hr.fer.zemris.jcms.beans.GroupBean;
import hr.fer.zemris.jcms.beans.ext.BaseUserBean;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.parsers.json.JSONException;
import hr.fer.zemris.jcms.parsers.json.JSONWriter;
import hr.fer.zemris.jcms.service2.course.groups.GroupOwnersService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.SubgroupOwnersData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.builders.BuilderDefault;
import hr.fer.zemris.jcms.web.navig.builders.course.groups.ShowGroupTreeBuilder;
import hr.fer.zemris.util.InputStreamWrapper;

@WebClass(dataClass=SubgroupOwnersData.class, defaultNavigBuilder=BuilderDefault.class)
public class SubgroupOwners extends Ext2ActionSupport<SubgroupOwnersData> {

	private static final long serialVersionUID = 1L;
	private InputStreamWrapper streamWrapper;

	@WebMethodInfo(
		dataResultMappings={
			@DataResultMapping(dataResult=AbstractActionData.RESULT_INPUT,struts2Result=INPUT,registerDelayedMessages=false)
		},
		struts2ResultMappings={
			@Struts2ResultMapping(struts2Result=INPUT, navigBuilder=ShowGroupTreeBuilder.class, navigBuilderIsRoot=false, additionalMenuItems={"m2","Navigation.groupOwners"})
		}
	)
	public String show() {
		GroupOwnersService.prepareShow(getEntityManager(), data);
		return null;
	}

	@WebMethodInfo(
		dataResultMappings={
			@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=WRAPPED_STREAM,registerDelayedMessages=false),
			@DataResultMapping(dataResult=AbstractActionData.RESULT_FATAL,struts2Result=WRAPPED_STREAM,registerDelayedMessages=false)
		},
		struts2ResultMappings={
			@Struts2ResultMapping(struts2Result=WRAPPED_STREAM, navigBuilder=BuilderDefault.class)
		}
	)
	public String currentJSON() throws JSONException, IOException {
		GroupOwnersService.current(getEntityManager(), data);
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			StringWriter writer = new StringWriter();
			JSONWriter jw = new JSONWriter(writer).object();
			jw.key("status").value("ERR");
			jw.endObject();
			streamWrapper = InputStreamWrapper.createInputStreamWrapperFromText(
					writer.toString(), 
    				"application/json");
    		return null;
		}
		StringWriter writer = new StringWriter();
		JSONWriter jw = new JSONWriter(writer).object().key("glist");
		serializeGroupList(jw, data.getGroupList());
		jw.key("status").value("OK");
		jw.key("groupID").value(data.getGroup().getId());
		jw.key("users");
		serializeUsersList(jw, data.getUserList());
		jw.endObject();
		streamWrapper = InputStreamWrapper.createInputStreamWrapperFromText(
				writer.toString(), 
				"application/json");
		return null;
	}

	@WebMethodInfo(
		dataResultMappings={
			@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=WRAPPED_STREAM,registerDelayedMessages=false),
			@DataResultMapping(dataResult=AbstractActionData.RESULT_FATAL,struts2Result=WRAPPED_STREAM,registerDelayedMessages=false)
		},
		struts2ResultMappings={
			@Struts2ResultMapping(struts2Result=WRAPPED_STREAM, navigBuilder=BuilderDefault.class)
		}
	)
	public String addGroupOwnerJSON() throws IOException, JSONException {
		GroupOwnersService.addGroupOwner(getEntityManager(), data);
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			StringWriter writer = new StringWriter();
			JSONWriter jw = new JSONWriter(writer).object();
			jw.key("status").value("ERR");
			jw.endObject();
			streamWrapper = InputStreamWrapper.createInputStreamWrapperFromText(
					writer.toString(), 
    				"application/json");
		} else {
			StringWriter writer = new StringWriter();
			JSONWriter jw = new JSONWriter(writer).object();
			jw.key("status").value("OK");
			jw.key("owner").object();
			// Ovdje stavi korisnika
			jw.key("firstName").value(data.getOwner().getUser().getFirstName());
			jw.key("lastName").value(data.getOwner().getUser().getLastName());
			jw.key("jmbag").value(data.getOwner().getUser().getJmbag());
			jw.key("userID").value(data.getOwner().getId());
			jw.endObject();
			jw.endObject();
			streamWrapper = InputStreamWrapper.createInputStreamWrapperFromText(
					writer.toString(), 
    				"application/json");
		}
		return null;
	}
	
	@WebMethodInfo(
		dataResultMappings={
			@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=WRAPPED_STREAM,registerDelayedMessages=false),
			@DataResultMapping(dataResult=AbstractActionData.RESULT_FATAL,struts2Result=WRAPPED_STREAM,registerDelayedMessages=false)
		},
		struts2ResultMappings={
			@Struts2ResultMapping(struts2Result=WRAPPED_STREAM, navigBuilder=BuilderDefault.class)
		}
	)
	public String removeGroupOwnerJSON() throws JSONException, IOException {
		GroupOwnersService.removeGroupOwner(getEntityManager(), data);
		if (data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			StringWriter writer = new StringWriter();
			JSONWriter jw = new JSONWriter(writer).object();
			jw.key("status").value("ERR");
			jw.endObject();
			streamWrapper = InputStreamWrapper.createInputStreamWrapperFromText(
					writer.toString(), 
    				"application/json");
		} else {
			StringWriter writer = new StringWriter();
			JSONWriter jw = new JSONWriter(writer).object();
			jw.key("status").value("OK");
			jw.endObject();
			streamWrapper = InputStreamWrapper.createInputStreamWrapperFromText(
					writer.toString(), 
    				"application/json");
		}
		return null;
	}
	
	public InputStreamWrapper getStreamWrapper() {
		return streamWrapper;
	}
	public void setStreamWrapper(InputStreamWrapper streamWrapper) {
		this.streamWrapper = streamWrapper;
	}
	
	private JSONWriter serializeUsersList(JSONWriter jw, List<User> userList) throws JSONException {
		jw.array();
		for(User u : userList) {
			jw.object();
			jw.key("firstName").value(u.getFirstName());
			jw.key("lastName").value(u.getLastName());
			jw.key("jmbag").value(u.getJmbag());
			jw.key("userID").value(u.getId());
			jw.endObject();
		}
		jw.endArray();
		return jw;
	}

	private JSONWriter serializeGroupList(JSONWriter jw, List<GroupBean> groupList) throws JSONException {
		jw.array();
		for(GroupBean gb : groupList) {
			jw.object();
			jw.key("groupID").value(gb.getId());
			jw.key("groupName").value(gb.getName());
			jw.key("owners");
			serializeOwnersList(jw, gb.getOwnerList());
			jw.endObject();
		}
		jw.endArray();
		return jw;
	}

	private JSONWriter serializeOwnersList(JSONWriter jw, List<BaseUserBean> ownerList) throws JSONException {
		jw.array();
		for(BaseUserBean u : ownerList) {
			jw.object();
			jw.key("firstName").value(u.getFirstName());
			jw.key("lastName").value(u.getLastName());
			jw.key("jmbag").value(u.getJmbag());
			jw.key("userID").value(u.getUserID());
			jw.endObject();
		}
		jw.endArray();
		return jw;
	}
	
	public Long getGroupID() {
		return data.getGroupID();
	}
	
	public void setGroupID(Long groupID) {
		data.setGroupID(groupID);
	}
}
