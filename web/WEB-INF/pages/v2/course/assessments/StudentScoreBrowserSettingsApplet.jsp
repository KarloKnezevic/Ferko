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

    <h1>Podešavanje prikaza bodova studentima</h1>
    <div>Ova stranica zahtjeva omogućeno izvođenje Java appleta.</div>

	<applet code="hr.fer.zemris.sscoretree.MainApplet" archive="sscoretree/ferko-sscoretree.jar" width="800" height="600">
	  <param name="ferkourl" value="<s:url action="StudentScoreBrowserSettings" forceAddSchemeHostAndPort="true" />">
	  <param name="courseInstanceID" value="<s:property value="data.courseInstance.id"/>">
	</applet>

</div>
