<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<h2>Postavke web stranica kolegija</h2>
<s:form action="CourseParameters2" method="POST" theme="ferko">
	<s:checkbox name="data.wikiEnabled" value="%{data.wikiEnabled}" label="%{getText('Settings.wikiEnabled')}"/>
	<s:hidden theme="simple" name="courseInstanceID" value="%{data.courseInstance.id}"/>
	<s:submit theme="simple" value="%{getText('forms.general.update')}" method="update" />
</s:form>

<br><br><br>