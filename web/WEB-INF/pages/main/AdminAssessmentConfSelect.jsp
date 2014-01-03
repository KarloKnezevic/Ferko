<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
  <ul>
  <s:iterator value="data.messageLogger.messages">
      <li>[<s:property value="messageType"/>]<s:property value="messageText"/></li>
  </s:iterator>
  </ul>
</s:if>

<s:if test="data != null && data.courseInstance != null">

  <div><b>Promjena vrste provjere za: <s:property value="data.assessment.name"/></b></div>

  <div>Jeste li sigurni da želite promjeniti vrstu provjere? Promjenom vrste provjere eventualni podaci pohranjeni uz staru vrstu provjere bit će izgubljeni.</div>

  <s:form action="AdminAssessmentConfSelect"  theme="ferko">
    <s:hidden name="confSelectorID" value="%{confSelectorID}"></s:hidden>
    <s:hidden name="courseInstanceID" value="%{data.courseInstance.id}"></s:hidden>
    <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
    <s:submit method="changeIt" value="Da, siguran sam!"></s:submit>
  </s:form>

<div class="bottomNavMenu">
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="AdminAssessmentList"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.assessments"/></a>
 | <a href="<s:url action="AdminAssessmentView"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param></s:url>"><s:text name="Navigation.details"/></a>
</div>

</s:if>
