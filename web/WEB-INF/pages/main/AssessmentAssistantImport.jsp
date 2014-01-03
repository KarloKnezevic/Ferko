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

<div><a href="<s:url action="AssessmentAssistantSchedule" method="editAssistants"><s:param name="assessmentID" value="data.assessment.id"/></s:url>"><s:text name="Navigation.goBack"/></a>

<s:form action="AssessmentAssistantSchedule" method="post">
<s:textarea rows="20" cols="80" name="importData" label="%{getText('forms.assistantJmbagImport')}" />
<s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
<s:submit method="importAssistants"/>
</s:form>
<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="AdminAssessmentList"><s:param name="courseInstanceID" value="data.courseInstance.id"/></s:url>"><s:text name="Navigation.assessments"/></a>
 | <a href="<s:url action="AssessmentSchedule"><s:param name="assessmentID" value="assessmentID"/></s:url>"><s:text name="Navigation.assessmentScheduleEdit"/></a>
 | <a href="<s:url action="AssessmentAssistantSchedule" method="editAssistants"><s:param name="assessmentID" value="data.assessment.id"/></s:url>"><s:text name="Navigation.assistantSchedule"/></a>
</div> 
</div>