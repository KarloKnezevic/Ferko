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

    <div> 
	<s:form action="SeminarRootEdit" method="post" theme="ferko">
		<s:hidden name="data.id"></s:hidden>
		<s:textfield name="data.yearSemester"  label="%{getText('forms.yearSemester')}"></s:textfield>
		<s:textfield name="data.groupName"  label="%{getText('forms.groupName')}"></s:textfield>
		<s:textfield name="data.source"  label="%{getText('forms.source')}"></s:textfield>
		<s:checkbox name="data.active"  label="%{getText('forms.active')}"></s:checkbox>
		<s:submit method="saveSeminarRoot"></s:submit>
	</s:form>
    </div>

</div>
