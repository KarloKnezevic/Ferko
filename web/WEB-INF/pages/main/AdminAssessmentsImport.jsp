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

<s:if test="data.getAllAssessmentTags()!=null && data.getAllSemesters()!=null">
	<s:form action="AdminAssessmentsImport" method="post" theme="ferko">
		<s:select list="data.allAssessmentTags" listKey="shortName"  listValue="name" name="assessmentTag" required="true" label="%{getText('forms.assessmentTag')}" />
		<s:select list="data.allSemesters" listKey="id" listValue="fullTitle" name="currentYearSemesterID" required="true" label="%{getText('forms.Semester')}" />
		<s:textarea rows="20" cols="80" name="assessmentsImport" label="%{getText('forms.assessmentsImport')}" required="true" />
		<s:submit method="update" />
	</s:form>
</s:if>

</div>
