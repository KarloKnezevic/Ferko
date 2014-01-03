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
<h2><s:property value="data.roomName"/></h2>
<div>Detalji: </div>
<table>
	<thead>
	<tr>
	  <th colspan="2">Asistenti</th>
	</tr>
	<tr>
	  <th>Prezime</th>
      <th>Ime</th>
	</tr>
	</thead>
	<tbody>
	<s:if test="data.assistantList != null && data.assistantList.size()>0">
	<s:iterator value="data.assistantList" status="stat">
    <tr class="<s:if test="#stat.odd == true">oddrow</s:if><s:else>evenrow</s:else>">
	  <td><s:property value="lastName"/></td>
	  <td><s:property value="firstName"/></td>
	</tr>
	</s:iterator>
	</s:if>
	<s:else>
	<tr>
	  <td colspan="2">Nema asistenata</td>
	</tr>
	</s:else>
	</tbody>
</table>
<table>
    <thead>
    <tr>
      <th colspan="4">Studenti</th>
    <tr>
      <th>RB.</th>
      <th>Jmbag</th>
      <th>Prezime</th>
      <th>Ime</th>
    </tr>
    </thead>
    <tbody>
    <s:if test="data.userList != null && data.userList.size()>0">
    <s:iterator value="data.userList" status="stat">
    <tr class="<s:if test="#stat.odd == true">oddrow</s:if><s:else>evenrow</s:else>">
      <td><s:property value="%{#stat.index+1}"/> </td>
      <td><s:property value="jmbag"/></td>
      <td><s:property value="lastName"/></td>
      <td><s:property value="firstName"/></td>
    </tr>
    </s:iterator>
    </s:if>
    <s:else>
	<tr>
	  <td colspan="4">Nema studenata</td>
	</tr>
	</s:else>
    </tbody>
</table>
<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="AdminAssessmentList"><s:param name="courseInstanceID" value="data.courseInstance.id"/></s:url>"><s:text name="Navigation.assessments"/></a>
 | <a href="<s:url action="AssessmentSchedule"><s:param name="assessmentID" value="data.assessment.id"/></s:url>"><s:text name="Navigation.assessmentScheduleEdit"/></a>
</div>
</div>