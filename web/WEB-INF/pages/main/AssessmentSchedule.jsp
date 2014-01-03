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

<div>
Uređivanje soba
<ul>
  <li>
    <a href="<s:url action="AssessmentRoomSchedule" method="editRooms"><s:param name="assessmentID" value="data.assessment.id"/></s:url>"><s:text name="Navigation.editRooms"/></a>
  </li>
</ul>
Uređivanje studenata
<ul>
  <li>
    <a href="<s:url action="AssessmentStudentSchedule" method="synchronizeStudents"><s:param name="assessmentID" value="data.assessment.id"/></s:url>"><s:text name="Navigation.synchronizeStudents"/></a>
  </li>
  <li>
    <a href="<s:url action="AssessmentStudentSchedule" method="makeStudentSchedule"><s:param name="assessmentID" value="data.assessment.id"/><s:param name="type">sorted</s:param></s:url>">
    <s:text name="Navigation.makeStudentScheduleSorted"/></a>
  </li>
  <li>
    <a href="<s:url action="AssessmentStudentSchedule" method="makeStudentSchedule"><s:param name="assessmentID" value="data.assessment.id"/><s:param name="type">random</s:param></s:url>">
    <s:text name="Navigation.makeStudentScheduleRandom"/></a>
  </li>
</ul>
Uređivanje asistenata
<ul>
  <li>
    <a href="<s:url action="AssessmentAssistantSchedule" method="editAssistants"><s:param name="assessmentID" value="data.assessment.id"/></s:url>"><s:text name="Navigation.assistantSchedule"/></a>
  </li>
  <li>
    <a href="<s:url action="AssessmentAssistantSchedule" method="editAssistantsSchedule"><s:param name="assessmentID" value="data.assessment.id"/></s:url>"><s:text name="Navigation.assistantRoomSchedule"/></a>
  </li>
</ul>

PDF datoteke
<ul>
  <li>
    <a href="<s:url action="AssessmentSchedule" method="downloadListings"><s:param name="assessmentID" value="data.assessment.id"/></s:url>"><s:text name="Navigation.assessmentListings"/></a>
  </li>
  <li>
    <a href="<s:url action="AssessmentSchedule" method="downloadSchedule"><s:param name="assessmentID" value="data.assessment.id"/></s:url>"><s:text name="Navigation.downloadAssessmentSchedule"/></a>
  </li>
</ul>

MailMerge datoteka
<ul>
  <li>
    <a href="<s:url action="AssessmentSchedule" method="downloadMailMerge"><s:param name="assessmentID" value="data.assessment.id"/></s:url>"><s:text name="Navigation.assessmentMailMerge"/></a>
  </li>
</ul>

<a href="<s:url action="AssessmentStudentSchedule" method="importScheduleEdit"><s:param name="assessmentID" value="data.assessment.id"/></s:url>"><s:text name="Navigation.importSchedule"/></a>
| <a href="<s:url action="AssessmentStudentSchedule" method="broadcastEvents"><s:param name="assessmentID" value="data.assessment.id"/></s:url>"><s:text name="Navigation.broadcastEvents"/></a>
</div>
<div>Raspored: </div>
<s:if test="data.roomList != null && data.roomList.size()>0">
<table>
    <thead>
    <tr>
      <th>Naziv dvorane</th>
      <th>Kapacitet</th>
      <th>Broj dodijeljenih studenata</th>
      <th>Broj dodijeljenih/potrebnih asistenata</th>
      <th>Detalji</th>
    </tr>
    </thead>
    <tbody>
    <s:iterator value="data.roomList" status="cust_stat">
    <tr class="<s:if test="#cust_stat.odd == true">oddrow</s:if><s:else>evenrow</s:else>">
      <td><s:property value="roomName"/></td>
      <td><s:property value="capacity"/></td>
      <td><s:property value="userNum"/></td>
      <td><s:property value="assistantNum"/>/<s:property value="assistantRequired"/></td>
      <td><a href="<s:url action="AssessmentSchedule" method="viewRoomInfo"><s:param name="assessmentRoomID" value="assessmentRoomID"/></s:url>"><s:text name="Navigation.details"/></a></td>
    </tr>
    </s:iterator>
    </tbody>
  </table>
</s:if>
<s:else>
<div>Trenutno ne postoji raspored</div>
</s:else>
<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="AdminAssessmentList"><s:param name="courseInstanceID" value="data.courseInstance.id"/></s:url>"><s:text name="Navigation.assessments"/></a>
</div>
</div>