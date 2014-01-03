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

    <h1>Analiza rasporeda studenata</h1>
    <div>Ova stranica zahtjeva omogućeno izvođenje Java appleta.</div>

	<applet code="hr.fer.zemris.jcms.occvis.MainApplet" archive="occviz/ferko-occviz.jar" width="800" height="600">
	  <param name="ferkourl" value="<s:url action="StudentScheduleAnalyzer" method="viewForSemesterAndUsers" forceAddSchemeHostAndPort="true" />">
	  <param name="dateFrom" value="<s:property value="data.dateFrom"/>">
	  <param name="dateTo" value="<s:property value="data.dateTo"/>">
	  <param name="courseInstanceID" value="<s:property value="data.courseInstance.id"/>">
	  <param name="jmbagsList" value="<s:property value="data.jmbagsSingleLine"/>">
	</applet>

</div>
