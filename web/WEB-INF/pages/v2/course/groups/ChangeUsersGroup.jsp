<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<h3><s:property value="data.userGroup.user.lastName"/>, <s:property value="data.userGroup.user.firstName"/> (<s:property value="data.userGroup.user.jmbag"/>)</h3>
<p>Trenutna grupa je: <s:property value="data.userGroup.group.name"/></p>

<s:form action="ChangeUsersGroup" method="post" theme="ferko">
	<s:select name="data.toGroupID" list="data.offeredGroups" listKey="id" listValue="name" required="true" label="%{getText('forms.general.destinationGroup')}" />
	<s:hidden name="data.groupID" value="%{data.userGroup.group.id}" />
	<s:hidden name="data.ugID" value="%{data.userGroup.id}" />
	<s:hidden name="data.mpID" value="%{data.marketPlaceGroup.id}" />
	<s:hidden name="data.viewedGroupID" value="%{data.viewedGroupID}" />
	<s:hidden name="data.lid" value="%{data.courseInstance.id}" />
	<s:submit method="change" value="%{getText('forms.confirm.changeGroup')}"/>
</s:form>
