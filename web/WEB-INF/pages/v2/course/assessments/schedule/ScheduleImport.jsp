<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<s:form action="AssessmentSchedule" method="POST" theme="ferko">
	<s:select list="data.importTypes" name="data.type" value="data.type" label="%{getText('forms.importType')}"/>
	<s:textarea rows="20" cols="80" name="data.scheduleImport" label="%{getText('forms.scheduleImport')}" required="true"></s:textarea>
	<s:hidden name="data.assessmentID" value="%{data.assessment.id}"></s:hidden>
	<s:submit method="importScheduleUpdate" value="%{getText('forms.general.upload')}" />
</s:form>
