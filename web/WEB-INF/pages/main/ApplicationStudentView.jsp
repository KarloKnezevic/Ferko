<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
  <ul class="msgList">
  <s:iterator value="data.messageLogger.messages">
      <li>[<s:property value="messageType"/>]<s:property value="messageText"/></li>
  </s:iterator>
  </ul>
</s:if>

<s:if test="data != null && data.courseInstance != null">
<s:if test="data.application == null">
  <p class="emptyMsg">Odabrali ste nepostojeću prijavu.</p>
</s:if>
<s:else>
	<h2><s:property value="data.application.applicationDefinition.name"/> (<s:property value="data.application.applicationDefinition.shortName"/>)</h2>
	<ul>
		<li>Prijava zaprimljena: <s:property value="data.sdf.format(data.application.date)"/></li>
		<li>Navedeni razlog: <s:property value="data.application.reason"/></li>
		<li>Status prijave:  <s:property value="data.statuses.get(data.application.status)"/></li> 		
		<s:if test="data.application.statusReason != null">
		<li>Obrazloženje: <s:property value="data.application.statusReason"/></li>
		</s:if>
	</ul>
</s:else>

<p><a href="<s:url action="ApplicationMain"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.applicationsHome"/></a></p>
</s:if>

