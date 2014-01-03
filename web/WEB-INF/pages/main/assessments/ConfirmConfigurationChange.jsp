<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

    <div>Promjena vrste provjere uklonit će iz sustava sve podatke definirane za staru vrstu provjere. Jeste li sigurni da to želite napraviti?</div>
    <s:form action="AdminAssessmentConfSelect">
      <s:hidden name="confSelectorID" value="confSelectorID"></s:hidden>
      <s:hidden name="courseInstanceID" value="data.courseInstance.id"></s:hidden>
      <s:hidden name="assessmentID" value="data.assessment.id"></s:hidden>
      <s:submit method="changeIt">Siguran sam, nastavi!</s:submit>
    </s:form>

<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="AdminAssessmentList"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.assessments"/></a>
 | <a href="<s:url action="AdminAssessmentView"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param></s:url>"><s:text name="Navigation.backToDetails"/></a>
</div>

</s:if>

</div>
