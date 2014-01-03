<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

<s:if test="data.messageLogger.hasMessages()">
  <ul>
  <s:iterator value="data.messageLogger.messages">
      <li>[<s:property value="messageType"/>]<s:property value="messageText"/></li>
  </s:iterator>
  </ul>
</s:if>

<s:form action="AssessmentStudentSchedule" method="POST">
	<s:select list="importTypes" name="type" value="type" label="%{getText('forms.importType')}"/>
	<s:textarea rows="20" cols="80" name="scheduleImport" label="%{getText('forms.scheduleImport')}" required="true"></s:textarea>
	<s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
	<s:submit method="importScheduleUpdate"></s:submit>
</s:form>

<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="AdminAssessmentList"><s:param name="courseInstanceID" value="data.courseInstance.id"/></s:url>"><s:text name="Navigation.assessments"/></a>
 | <a href="<s:url action="AssessmentSchedule"><s:param name="assessmentID" value="assessmentID"/></s:url>"><s:text name="Navigation.assessmentScheduleEdit"/></a>
</div>

</div>