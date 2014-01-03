<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<s:if test="data.application == null">
  <p class="emptyMsg">Odabrali ste nepostojeću prijavu.</p>
</s:if>
<s:else>
	<h2><s:property value="data.application.applicationDefinition.name"/> (<s:property value="data.application.applicationDefinition.shortName"/>)</h2>
	<ul>
		<li>Prijava zaprimljena: <s:property value="data.formatDateTime(data.application.date)"/></li>
		
		
<s:if test="data.bean.elements==null">
		<li>Navedeni razlog: <s:property value="data.application.reason"/></li>
</s:if><s:else>
<s:iterator value="data.bean.elements" status="itstat">
<s:if test="kind==3">
	<li><s:property value="text"/></li>
</s:if><s:elseif test="kind==2">
	<li><b><s:property value="text"/></b><br><s:property value="data.bean.map[renderingData]"/></li>
</s:elseif><s:elseif test="kind==1">
	<li><b><s:property value="text"/></b><br>
	<s:if test="getOption(data.bean.map[renderingData].key).isOther()">
	<s:property value="getOption(data.bean.map[renderingData].key).value"/>: <i><s:property value="data.bean.map[renderingData].text"/></i>
	</s:if><s:else>
	<s:property value="getOption(data.bean.map[renderingData].key).value"/>
	</s:else>
	</li>
</s:elseif><s:elseif test="kind==4">
	<li><b><s:property value="text"/></b><br><s:property value="data.bean.map[renderingData]"/></li>
</s:elseif>
</s:iterator>
</s:else>

		<li><hr width="80%"></li>
		<li>Status prijave:  <s:property value="data.statuses.get(data.application.status)"/></li> 		
		<s:if test="data.application.statusReason != null">
		<li>Obrazloženje: <s:property value="data.application.statusReason"/></li>
		</s:if>
	</ul>
</s:else>
