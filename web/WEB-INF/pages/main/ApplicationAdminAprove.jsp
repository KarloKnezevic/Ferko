<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<h2><s:property value="data.student.username"/> (<s:property value="data.student.firstName "/> <s:property value="data.student.lastName"/>)</h2>

<s:if test="data.beans.isEmpty()">
  <p class="emptyMsg">Student nema ispunjenih prijava.</p>
</s:if>
<s:else>
<s:form action="ApplicationAdminAprove" theme="ferko">
	<s:iterator value="data.beans" status="stat">
  		<li><s:property value="definition"/></li>
		<li>Prijava zaprimljena: <s:date name="date" format="%{getText('locale.datetime')}"/></li>
  		<li>Razlog: <s:property value="reason"/></li>
  		<s:textarea name="data.beans[%{#stat.index}].statusReason" label="%{getText('forms.reason')}" cols="50" rows="10" />
  		<s:radio name="data.beans[%{#stat.index}].status" list="data.statuses" />
		<s:hidden name="data.beans[%{#stat.index}].id" />
		<s:hidden name="data.beans[%{#stat.index}].definition" />
		<s:hidden name="data.beans[%{#stat.index}].date" />
		<s:hidden name="data.beans[%{#stat.index}].reason" />
	</s:iterator>
    <s:if test="data.fromDefinitionID!=null">
		<s:hidden name="data.fromDefinitionID" />
    </s:if>
	<s:hidden name="data.courseInstanceID" />
	<s:hidden name="data.studentID" />
	<s:submit method="aprove" />
</s:form>
</s:else>

<p><a href="<s:url action="ApplicationMain"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.applicationsHome"/></a></p>
