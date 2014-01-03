<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

    <div>Nastavkom ove akcije uklonit će se stari raspored. Jeste li sigurni da to želite napraviti?</div>
    <s:form action="AssessmentRoomSchedule">
      <s:hidden name="assessmentID" value="%{assessmentID}" />
      <s:hidden name="doit" value="true" />
      <s:iterator value="roomList" status="stat">
	    <s:hidden name="roomList[%{#stat.index}].id" value="%{id}"/>
		<s:hidden name="roomList[%{#stat.index}].capacity" value="%{capacity}" />
		<s:hidden name="roomList[%{#stat.index}].requiredAssistants" value="%{requiredAssistants}" />
		<s:hidden name="roomList[%{#stat.index}].roomTag" value="%{roomTag}"/>
		<s:hidden name="roomList[%{#stat.index}].taken" value="%{taken}" />
      </s:iterator>
      <s:submit method="updateRooms">Siguran sam, nastavi!</s:submit>
    </s:form>
<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="AdminAssessmentList"><s:param name="courseInstanceID" value="data.courseInstance.id"/></s:url>"><s:text name="Navigation.assessments"/></a>
 | <a href="<s:url action="AssessmentSchedule"><s:param name="assessmentID" value="assessmentID"/></s:url>"><s:text name="Navigation.assessmentScheduleEdit"/></a>
 | <a href="<s:url action="AssessmentRoomSchedule"><s:param name="assessmentID" value="assessmentID"/></s:url>"><s:text name="Navigation.editRooms"/></a>
</div>    
</div>
