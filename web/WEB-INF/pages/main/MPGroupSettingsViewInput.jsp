<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

	<s:if test="data.messageLogger.hasMessages()">
		<ul>
			<s:iterator value="data.messageLogger.messages">
				<li>[<s:property value="messageType" />] <s:property
					value="messageText" /></li>
			</s:iterator>
		</ul>
	</s:if>

	<s:form action="MPGroupSettingsView" method="get" theme="ferko">
		<s:select name="semesterID"  label="%{getText('forms.Semester')}" list="data.allSemesters" listKey="id" listValue="fullTitle"></s:select>
		<s:hidden name="parentRelativePath"></s:hidden>
		<s:submit method="view"></s:submit>
	</s:form>

</div>
