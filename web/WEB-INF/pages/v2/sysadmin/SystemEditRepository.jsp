<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

<h2><s:text name="EditRepository.title" /></h2>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
	<s:iterator value="data.messageLogger.messages">
		<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
	</s:iterator>
	</ul>
</s:if>

<s:form action="EditRepository" method="post" theme="ferko">
	<s:iterator value="repository" status="stat">
		<s:hidden name="repository[%{#stat.index}].key" value="%{key}"/>
		<s:textfield name="repository[%{#stat.index}].value" value="%{value}" label="%{key}"/>
	</s:iterator>
	<s:if test="!repository.isEmpty()">
		<s:submit value="%{getText('forms.update')}"/>
	</s:if>
	<s:textfield key="newName" label="%{getText('forms.newName')}"/>
	<s:textfield key="newValue" label="%{getText('forms.newValue')}"/>
	<s:submit method="addNew" value="%{getText('forms.add')}"/>
</s:form>

<div><a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000002</s:param></s:url>"><s:text name="Navigation.help"/></a></div>

</div>
