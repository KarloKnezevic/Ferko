<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

<s:if test="data.messageLogger.hasMessages()">
  <ul>
  <s:iterator value="data.messageLogger.messages">
      <li>[<s:property value="messageType"/>] <s:property value="messageText"/></li>
  </s:iterator>
  </ul>
</s:if>

<div><s:property value="data.courseInstance.course.name"/> (<s:property value="data.courseInstance.course.isvuCode"/>): <s:property value="data.assessment.name"/></div>
Popis asistenata:
<s:if test="assistantRoomBeanList != null && assistantRoomBeanList.size()>0">
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
				<s:iterator value="assistantRoomBeanList" status="stat">
    			<tr class="<s:if test="#stat.odd == true">oddrow</s:if><s:else>evenrow</s:else>">
					<td>
						<s:hidden name="assistantRoomBeanList[%{#stat.index}].assessmentScheduleID" value="%{assessmentScheduleID}"/>
						<s:property value="%{lastName}"/>
					</td>
					<td><s:property value="%{firstName}" /></td>
					<td><s:property value="%{jmbag}"/></td>
					<td><s:select listKey="id" listValue="name" list="data.roomList" name="assistantRoomBeanList[%{#stat.index}].assessmentRoomID" /></td>
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
<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="AdminAssessmentList"><s:param name="courseInstanceID" value="data.courseInstance.id"/></s:url>"><s:text name="Navigation.assessments"/></a>
 | <a href="<s:url action="AssessmentSchedule"><s:param name="assessmentID" value="assessmentID"/></s:url>"><s:text name="Navigation.assessmentScheduleEdit"/></a>
</div> 
</div>