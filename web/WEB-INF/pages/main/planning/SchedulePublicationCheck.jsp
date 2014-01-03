<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

	<s:if test="data.messageLogger.hasMessages()">
		<ul>
			<s:iterator value="data.messageLogger.messages">
				<li>[<s:property value="messageType" />] <s:property
					value="messageText" /></li>
			</s:iterator>
		</ul>
	</s:if>

	<br><br>
	
	<s:iterator value="data.scheduleValidationMessages">
		<br>
		<s:property/>
	</s:iterator>
	<br><br>
	<s:if test="data.scheduleValidationResult">
		<s:form theme="simple" action="SchedulePublication" method="post">
		<s:hidden value="%{courseInstanceID}" name="courseInstanceID"/>
		<s:hidden value="%{scheduleID}" name="scheduleID"/>
		<s:submit method="publish" type="button" label="%{getText('Planning.publishSchedule')}"></s:submit>
		</s:form>	
	</s:if>