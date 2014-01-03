<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<div class="content assessmentView">
	<div class="maincontent">
	<div class="inner-padding">
		<h2><s:property value="%{getText('Assessments.assessmentResults')}"/>: <s:property value="data.assessment.name"/> (<s:property value="data.assessment.shortName"/>)</h2>
		<s:if test="data.imposter">
		<s:property value="data.student.lastName"/>, <s:property value="data.student.firstName"/> (<s:property value="data.student.jmbag"/>)<br><br>
		</s:if>
		<s:include value="/WEB-INF/pages/main/assessments/AssessmentViewBodyContent.jsp"></s:include>
  </div>
  </div>
</div>
