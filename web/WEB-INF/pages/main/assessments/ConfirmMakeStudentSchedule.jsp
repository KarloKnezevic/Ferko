<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

    <div>Nastavkom ove akcije uklonit će se stari raspored. Jeste li sigurni da to želite napraviti?</div>
    <s:form action="AssessmentStudentSchedule">
      <s:hidden name="assessmentID" value="%{assessmentID}" />
      <s:hidden name="type" value="%{type}" />
      <s:hidden name="doit" value="true" />
      <s:submit method="makeStudentSchedule">Siguran sam, nastavi!</s:submit>
    </s:form>
<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="AdminAssessmentList"><s:param name="courseInstanceID" value="data.courseInstance.id"/></s:url>"><s:text name="Navigation.assessments"/></a>
 | <a href="<s:url action="AssessmentSchedule"><s:param name="assessmentID" value="assessmentID"/></s:url>"><s:text name="Navigation.assessmentScheduleEdit"/></a>
</div>
</div>
