<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<h2><s:text name="AssessmentFlags.nav.importValues" /> <a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000037</s:param></s:url>" onclick="blur();"><img src="img/icons/help.png"></a></h2>
<s:form action="AdminAssessmentFlagImport" theme="ferko">
<s:textarea name="text" label="%{getText('forms.data')}" rows="25" cols="40" />
<s:hidden name="id"></s:hidden>
<s:hidden name="courseInstanceID" value="%{data.courseInstance.id}"></s:hidden>
<s:submit value="%{getText('forms.general.update')}" method="importValues" />
</s:form>
