<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<div><a href="<s:url action="AssessmentAssistantSchedule" method="showImportAssistants"><s:param name="assessmentID" value="data.assessment.id"/></s:url>"><s:text name="Navigation.assistantImport"/></a></div>
<div align="right">Još potrebno asistenata: <s:property value="data.assistantsRequired"/></div>
Popis asistenata: 
<s:if test="data.assistantBeanList != null && data.assistantBeanList.size()>0">
	<s:form action="AssessmentAssistantSchedule" theme="simple">
	<table>
			<thead>
				<tr>
					<th>
						Prezime
					</th>
					<th>
						Ime
					</th>
					<th>
						Jmbag
					</th>
					<th>
						Odabrati
					</th>
				</tr>
			</thead>
			<tbody>
				<s:iterator value="data.assistantBeanList" status="stat">
 			    <tr class="<s:if test="#stat.odd == true">oddrow</s:if><s:else>evenrow</s:else>">
					<td>
						<s:hidden name="data.assistantBeanList[%{#stat.index}].userID" value="%{userID}"/>
						<s:property value="%{lastName}"/>
					</td>
					<td><s:property value="%{firstName}" /></td>
					<td><s:property value="%{jmbag}"/></td>
					<td><s:checkbox name="data.assistantBeanList[%{#stat.index}].taken" value="%{taken}" /></td>
				</tr>
				</s:iterator>
			</tbody>
	</table>
	<s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
	<s:submit method="updateAssistants"/>
	</s:form>
</s:if>
<s:else>
Nema asistenata za odabrati
</s:else>
