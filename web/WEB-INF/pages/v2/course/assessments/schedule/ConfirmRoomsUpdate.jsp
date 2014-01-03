<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<div>Nastavkom ove akcije uklonit će se stari raspored. Jeste li sigurni da to želite napraviti?</div>
<s:form action="AssessmentRoomSchedule" method="post" theme="ferko">
  <s:hidden name="assessmentID" value="%{assessmentID}" />
  <s:hidden name="data.doit" value="true" />
  <s:iterator value="data.roomList" status="stat">
  <s:hidden name="data.roomList[%{#stat.index}].id" value="%{id}"/>
  <s:hidden name="data.roomList[%{#stat.index}].capacity" value="%{capacity}" />
  <s:hidden name="data.roomList[%{#stat.index}].requiredAssistants" value="%{requiredAssistants}" />
  <s:hidden name="data.roomList[%{#stat.index}].roomTag" value="%{roomTag}"/>
  <s:hidden name="data.roomList[%{#stat.index}].taken" value="%{taken}" />
  </s:iterator>
  <s:submit method="updateRooms">Siguran sam, nastavi!</s:submit>
</s:form>
