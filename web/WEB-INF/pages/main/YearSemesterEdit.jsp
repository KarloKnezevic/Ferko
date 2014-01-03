<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

	<s:if test="data.messageLogger.hasMessages()">
		<ul class="msgList">
			<s:iterator value="data.messageLogger.messages">
				<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
			</s:iterator>
		</ul>
	</s:if>

	<h1>Dodavanje/uređivanje semestra</h1>

	<s:form action="YearSemesterEdit" method="post" theme="ferko">
		<s:textfield name="bean.id"  label="%{getText('forms.id')}" />
		<s:textfield name="bean.academicYear" label="%{getText('forms.academicYear')}" />
		<s:textfield name="bean.semester" label="%{getText('forms.semester')}" />
		<s:textfield name="bean.startsAt" label="%{getText('forms.startAt')}" />
		<s:textfield name="bean.endsAt" label="%{getText('forms.endsAt')}" />
		<s:hidden name="create" />
		<s:submit method="saveYS" />
	</s:form>

</div>
