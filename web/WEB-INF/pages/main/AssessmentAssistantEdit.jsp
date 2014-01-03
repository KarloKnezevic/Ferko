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
<br>
<div><a href="<s:url action="AssessmentAssistantSchedule" method="showImportAssistants"><s:param name="assessmentID" value="data.assessment.id"/></s:url>"><s:text name="Navigation.assistantImport"/></a></div>
<div align="right">Jos potrebno asistenata: <s:property value="data.assistantsRequired"/></div>
Popis asistenata: 
<s:if test="assistantBeanList != null && assistantBeanList.size()>0">
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
				<s:iterator value="assistantBeanList" status="stat">
 			    <tr class="<s:if test="#stat.odd == true">oddrow</s:if><s:else>evenrow</s:else>">
					<td>
						<s:hidden name="assistantBeanList[%{#stat.index}].userID" value="%{userID}"/>
						<s:property value="%{lastName}"/>
					</td>
					<td><s:property value="%{firstName}" /></td>
					<td><s:property value="%{jmbag}"/></td>
					<td><s:checkbox name="assistantBeanList[%{#stat.index}].taken" value="%{taken}" /></td>
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
<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="AdminAssessmentList"><s:param name="courseInstanceID" value="data.courseInstance.id"/></s:url>"><s:text name="Navigation.assessments"/></a>
 | <a href="<s:url action="AssessmentSchedule"><s:param name="assessmentID" value="assessmentID"/></s:url>"><s:text name="Navigation.assessmentScheduleEdit"/></a>
</div> 
</div>