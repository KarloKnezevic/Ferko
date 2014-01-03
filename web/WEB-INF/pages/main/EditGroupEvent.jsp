<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.bean.id==null">
  <h2>Definiranje novog događaja</h2>
</s:if>
<s:else>
  <h2>Uređivanje događaja</h2>
</s:else>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<s:form action="EditGroupEvent" theme="ferko">
	<s:textfield name="bean.title" label="%{getText('forms.title')}" />
	<s:textfield name="bean.start" label="%{getText('forms.startAt')}" />
	<s:textfield name="bean.duration" label="%{getText('forms.duration')}" />
	<s:select list="data.rooms" listKey="id" listValue="name" name="bean.roomID" label="%{getText('forms.room')}"></s:select>
	<s:hidden name="bean.id" />
	<s:hidden name="courseInstanceID" value="%{data.courseInstance.id}" />
	<s:hidden name="relativePath" />
	<s:submit method="saveEvent" />
</s:form>

<div>
<a href="<s:url action="ListGroupEvents"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="relativePath"><s:property value="relativePath"/></s:param></s:url>"><s:text name="Navigation.listGroupEvents"/></a>
</div>
