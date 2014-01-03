<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<div style="font-weight: bold;">Naputak</div>
<div style="margin-bottom: 10px;">
 Na ovom mjestu možete napraviti unos popisa asistenata koji su se prijavili kroz Ahyco. Dovoljno je iz Ahyco-a
 tablicu dodijeljenih asistenata (nalazi se na stranici "Izvještaj s popisom dežurnih asistenata na provjeri") 
 kopirati u donju kutiju za unos teksta. Format će ispasti dosta ružan, ali nema
 brige; unos će automatski pokušati odgonetnuti o kojim se asistentima radi, i ako nešto ne uspije, javit će
 koji su problemi.
</div>

<s:form action="AssessmentAssistantSchedule" method="post" theme="ferko">
<s:textarea rows="20" cols="80" name="data.importData" label="%{getText('forms.assistantJmbagImport')}" />
<s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
<s:submit method="importAssistants"/>
</s:form>
