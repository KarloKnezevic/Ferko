<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data==null">
  <div>Nisam dobio niti data objekt.</div>
</s:if>
<s:elseif test="data.assessment.assessmentConfiguration==null">
  <div>Konfiguracija je null! Ovo se nije smjelo dogoditi!</div>
</s:elseif>
<s:else>
	<s:form action="AssessmentCreateAppeal" theme="simple" method="post">
      <s:hidden name="courseInstanceID" value="%{data.courseInstance.id}" />
      <s:hidden name="assessmentID" value="%{data.assessment.id}" />
      <label><s:property value="%{getText('appeal.notProcessed')}" /> </label><s:submit method="notProcessed" name="submit" value="%{getText('appeal.create')}" onclick="return confirm('%{getText('appeal.createOrNot')}')" />
	  <a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000023</s:param></s:url>"><s:text name="Navigation.help"/></a>
    </s:form>
</s:else>
