<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>
 
<h2><s:text name="Navigation.editGroup"></s:text></h2>

<s:form action="GroupEdit" theme="ferko">
	<s:textfield name="data.groupName" label="%{getText('forms.name')}" />
	<s:hidden name="data.groupID" />
	<s:submit method="save" />
</s:form>
