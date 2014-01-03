<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

Popis asistenata:
<s:if test="data.assistantRoomBeanList != null && data.assistantRoomBeanList.size()>0">
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
						Dvorana
					</th>
				</tr>
			</thead>
			<tbody>
				<s:iterator value="data.assistantRoomBeanList" status="stat">
    			<tr class="<s:if test="#stat.odd == true">oddrow</s:if><s:else>evenrow</s:else>">
					<td>
						<s:hidden name="data.assistantRoomBeanList[%{#stat.index}].assessmentScheduleID" value="%{assessmentScheduleID}"/>
						<s:property value="%{lastName}"/>
					</td>
					<td><s:property value="%{firstName}" /></td>
					<td><s:property value="%{jmbag}"/></td>
					<td><s:select listKey="id" listValue="name" list="data.roomList" name="data.assistantRoomBeanList[%{#stat.index}].assessmentRoomID" /></td>
				</tr>
				</s:iterator>			
			</tbody>
	</table>
	<s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
	<s:submit method="updateAssistantsSchedule"/>
	</s:form>
</s:if>
<s:else>
Nema asistenata
</s:else>
